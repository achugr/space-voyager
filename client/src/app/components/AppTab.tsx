import "./AppTab.css";
import {styled} from "@mui/material";

export interface AppTabProps {
    name: string;
    isActive: boolean;
    onClick: () => void;
}

export function AppTab(props: AppTabProps) {

    const TabTitle = styled('div')(({ theme }) => ({
        ...theme.typography.button,
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(1),
    }));

    return (
        <div onClick={() => props.onClick()}
             className={props.isActive ? `tab-container tab-container-active` : `tab-container tab-container-inactive`}>
            {/*<span className="tab-name">{props.name}</span>*/}
            <TabTitle>{props.name}</TabTitle>
        </div>
    )
}
