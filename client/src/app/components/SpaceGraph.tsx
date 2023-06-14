import React, {FC, useEffect, useRef} from "react";

import "@react-sigma/core/lib/react-sigma.min.css";
import {GraphData} from "../service/graphData";
import {httpGet} from "../service/utils";
import {useState} from "@hookstate/core";
import {UserTokenData} from "../service/spaceAuth";
import ForceGraph3d, {GraphData as ForceGraphData, LinkObject, NodeObject} from 'react-force-graph-3d';
import {Renderer} from 'three';
import {CustomCSS2DObject, CustomCSS2DRenderer} from "./CustomCSS2DRenderer";
import {getImage} from "../service/graphService";

export type GraphProps = {
    userTokenData: UserTokenData;
}

const SpaceGraph: FC<GraphProps> = (props: GraphProps) => {
    const ref = useRef();
    const graphDataState = useState(() => {
        return {
            loaded: false,
            forceGraphData: {} as ForceGraphData
        } as GraphState;
    })
    useEffect(() => {
        if (!graphDataState.loaded.get()) {
            updateGraphData();
        }
    })

    function getRandomInt(max: number) {
        return Math.floor(Math.random() * max);
    }

    function updateGraphData() {
        loadGraphData().then((value) => {
            let nodes: CustomNodeObject[] = value.vertices
                // .filter(vertex => vertex.label === "Employee" || vertex.label === "CalendarEvent")
                .map(vertex => ({
                    id: vertex.id,
                    name: vertex.name,
                    label: vertex.label,
                    x: getRandomInt(100),
                    y: getRandomInt(100),
                    z: getRandomInt(100)
                }));
            let links: LinkObject[] = value.edges.map(edge => ({
                source: edge.start,
                target: edge.end
            }));
            let forceGraphData = {nodes: nodes, links: links} as ForceGraphData;
            graphDataState.forceGraphData.set(forceGraphData);
            graphDataState.loaded.set(true);
        });
    }

    async function loadGraphData(): Promise<GraphData> {
        return httpGet(
            "/api/graph", props.userTokenData.userToken
        )
            .then((response) => response.json())
            .then(json => json as GraphData);
    }

    return (
        <>
            {
                graphDataState.loaded.get() &&
                <ForceGraph3d
                    extraRenderers={[new CustomCSS2DRenderer() as Renderer]}
                    graphData={graphDataState.forceGraphData.get({noproxy: true})}
                    backgroundColor="rgba(0,0,0,0)"
                    linkColor={() => "black"}
                    linkOpacity={0.5}
                    linkWidth="1px"
                    nodeThreeObject={(node: CustomNodeObject) => {
                        const nodeEl = document.createElement('div');
                        nodeEl.style.textAlign = 'center'

                        let image = document.createElement('img');
                        image.src = getImage(node.label!!);
                        nodeEl.appendChild(image);

                        let span = document.createElement('span');
                        span.textContent = node.name!!;
                        span.style.color = "black";
                        span.style.fontSize = "15";
                        span.className = 'label';
                        span.style.marginTop = '-1em';
                        span.style.display = 'block';
                        nodeEl.appendChild(span);
                        return new CustomCSS2DObject(nodeEl);
                    }}
                />
            }
        </>
    );
};

export default SpaceGraph;

type GraphState = {
    loaded: boolean,
    forceGraphData: ForceGraphData,
}

interface CustomNodeObject extends NodeObject {
    name?: string;
    label?: string;
}
