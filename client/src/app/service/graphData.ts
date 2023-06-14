export interface GraphData {
    vertices: Vertex[]
    edges: Edge[]
}

export interface Vertex {
    id: number
    label: string
    name: string
}

export interface Edge {
    id: number
    label: string
    name: string
    start: number
    end: number
}
