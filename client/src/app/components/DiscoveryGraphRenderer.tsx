import React, {FC, useEffect} from "react";

import "@react-sigma/core/lib/react-sigma.min.css";
import {MultiDirectedGraph} from "graphology";
import getNodeProgramImage from "sigma/rendering/webgl/programs/node.image";
import {
    ControlsContainer,
    FullScreenControl,
    SigmaContainer,
    useLoadGraph,
    useRegisterEvents, useSetSettings, useSigma,
    ZoomControl
} from "@react-sigma/core";
import {GraphData} from "../service/graphData";
import {httpGet} from "../service/utils";
import {useState} from "@hookstate/core";
import {LayoutForceAtlas2Control, useWorkerLayoutForceAtlas2} from "@react-sigma/layout-forceatlas2";

import {Attributes} from "graphology-types";
import Loader from "./Loader";
import {UserTokenData} from "../service/spaceAuth";
import {SearchBar} from "./SearchBar";
import {getImage} from "../service/graphService";

export type LoadGraphProps = {
    userTokenData: UserTokenData;
}

export type GraphProps = {
    graphData: GraphData
}

const DiscoveryGraphRenderer: FC<LoadGraphProps> = (props: LoadGraphProps) => {
    const state = useState(() => {
        return {
            loaded: false,
            graphData: {} as GraphData
        } as GraphState;
    })

    const Fa2: React.FC = () => {
        const {start, kill, isRunning} = useWorkerLayoutForceAtlas2({
            settings: {
                slowDown: 10,
            }
        });

        useEffect(() => {
            // start FA2
            start();
            return () => {
                // Kill FA2 on unmount
                kill();
            };
        }, [start, kill]);

        return null;
    };

    useEffect(() => {
        if (!state.loaded.get()) {
            updateGraphData();
        }
    },[state])

    const DiscoveryGraph: FC<GraphProps> = (props: GraphProps) => {

        const sigmaGraph = useState(() => {
            return new MultiDirectedGraph()
        })
        const registerEvents = useRegisterEvents();
        const loadGraph = useLoadGraph();
        const sigma = useSigma();
        const dragState = useState(() => {
            return {
                draggedNode: null
            } as DragState
        });
        const hoveredNode = useState(() => {
            return {id: null} as HoverState
        })
        const setSettings = useSetSettings();

        useEffect(() => {
            const graph = sigmaGraph.get();

            for (let i = 0; i < props.graphData.vertices.length; i++) {
                const vertex = props.graphData.vertices[i];
                if (!graph.hasNode(vertex.id)) {
                    graph.addNode(vertex.id, {
                        x: 250 + getRandomInt(100), y: 250 + getRandomInt(100), label: vertex.name, size: 10,
                        image: getImage(vertex.label),
                    })
                } else {
                    graph.updateNodeAttributes(vertex.id, (existingAttributes) => {
                        return {
                            x: existingAttributes.x,
                            y: existingAttributes.y,
                            label: vertex.name,
                            size: 10, ...existingAttributes
                        }
                    })
                }
            }
            for (let i = 0; i < props.graphData.edges.length; i++) {
                const edge = props.graphData.edges[i];
                if (!graph.hasEdge(edge.id)) {
                    graph.addEdgeWithKey(edge.id, edge.start, edge.end, {label: edge.name})
                } else {
                    graph.updateEdgeAttributes(edge.id, () => {
                        return {label: edge.name}
                    })
                }
            }
            registerEvents({
                // node events
                downNode: (e) => {
                    dragState.dragedNode.set(e.node);
                    sigma.getGraph().setNodeAttribute(e.node, "highlighted", true);
                },
                mouseup: (e) => {
                    let draggedNode = dragState.dragedNode.get();
                    if (draggedNode) {
                        dragState.dragedNode.set(null);
                        sigma.getGraph().removeNodeAttribute(draggedNode, "highlighted");
                    }
                },
                mousedown: (e) => {
                    // Disable the autoscale at the first down interaction
                    if (!sigma.getCustomBBox()) sigma.setCustomBBox(sigma.getBBox());
                },
                mousemove: (e) => {
                    if (dragState.dragedNode.get()) {
                        // Get new position of node
                        const pos = sigma.viewportToGraph(e);
                        sigma.getGraph().setNodeAttribute(dragState.dragedNode.get(), "x", pos.x);
                        sigma.getGraph().setNodeAttribute(dragState.dragedNode.get(), "y", pos.y);

                        // Prevent sigma to move camera:
                        e.preventSigmaDefault();
                        e.original.preventDefault();
                        e.original.stopPropagation();
                    }
                },
                touchup: (e) => {
                    if (dragState.dragedNode.get()) {
                        dragState.dragedNode.set(null);
                        sigma.getGraph().removeNodeAttribute(dragState.dragedNode, "highlighted");
                    }
                },
                touchdown: (e) => {
                    // Disable the autoscale at the first down interaction
                    if (!sigma.getCustomBBox()) sigma.setCustomBBox(sigma.getBBox());
                },
                touchmove: (e) => {
                    if (dragState.dragedNode.get()) {
                        // Get new position of node
                        const pos = sigma.viewportToGraph(e.touches[0]);
                        sigma.getGraph().setNodeAttribute(dragState.dragedNode.get(), "x", pos.x);
                        sigma.getGraph().setNodeAttribute(dragState.dragedNode.get(), "y", pos.y);

                        // Prevent sigma to move camera:
                        // e.preventSigmaDefault();
                        e.original.preventDefault();
                        e.original.stopPropagation();
                    }
                },
                // sigma kill
                kill: () => console.log("kill"),
                // sigma camera update
                updated: (event) => console.log("updated", event.x, event.y, event.angle, event.ratio),
                enterNode: (event) => hoveredNode.id.set(event.node),
                leaveNode: () => hoveredNode.id.set(null),
            })
            sigmaGraph.set(graph)
            loadGraph(graph);
        }, [loadGraph, registerEvents, state]);

        function getRandomInt(max: number) {
            return Math.floor(Math.random() * max);
        }

        useEffect(() => {
            setSettings({
                nodeReducer: (node, data) => {
                    const graph = sigma.getGraph();
                    const newData: Attributes = {...data, highlighted: data.highlighted || false};

                    if (hoveredNode.id.get()) {
                        if (node === hoveredNode.id.get() || graph.neighbors(hoveredNode.id.get()).includes(node)) {
                            newData.highlighted = true;
                        } else {
                            newData.color = "#E2E2E2";
                            newData.highlighted = false;
                        }
                    }
                    return newData;
                },
                edgeReducer: (edge, data) => {
                    const graph = sigma.getGraph();
                    const newData = {...data, hidden: false};

                    if (hoveredNode.id.get() && !graph.extremities(edge).includes(hoveredNode.id.get()!!)) {
                        newData.hidden = true;
                    }
                    return newData;
                },
            });
        }, [hoveredNode, setSettings, sigma]);

        return (
            <>
                {
                    !state.loaded.get() &&
                    <Loader style={{position: "absolute", top: "30%", left: "50%"}}/>
                }
            </>
        )
    };

    function updateGraphData() {
        loadGraphData().then((value) => {
            state.graphData.set(value);
            state.loaded.set(true);
        });
    }

    async function loadGraphData(): Promise<GraphData> {
        return httpGet(
            "/api/graph", props.userTokenData.userToken
        )
            .then((response) => response.json())
            .then(json => json as GraphData);
    }

    const handleChange = (event: React.SyntheticEvent) => {
        console.log("hello: " + event)
        updateGraphData();
    };

    return (
        <>
            <SearchBar userTokenData={props.userTokenData} inputHandler={handleChange}/>
            {
                state.loaded.get() &&
                <SigmaContainer
                    style={{
                        height: window.innerHeight,
                    }}
                    settings={{
                        nodeProgramClasses: {image: getNodeProgramImage()},
                        defaultNodeType: "image",
                        defaultNodeColor: "rgba(0, 0, 0, 0)"
                    }}
                >
                    <ControlsContainer position={"bottom-right"}>
                        <ZoomControl/>
                        <FullScreenControl/>
                        <LayoutForceAtlas2Control/>
                    </ControlsContainer>
                    <DiscoveryGraph graphData={state.graphData.get()}/>
                    <Fa2/>
                </SigmaContainer>
            }
        </>
    );
};

export default DiscoveryGraphRenderer;

type HoverState = {
    id?: string | null
}
type DragState = {
    dragedNode?: string | null
}
type GraphState = {
    loaded: boolean,
    graphData: GraphData,
}