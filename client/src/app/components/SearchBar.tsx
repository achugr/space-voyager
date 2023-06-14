import {UserTokenData} from "../service/spaceAuth";
import {Grid, IconButton, InputAdornment, TextField, Tooltip} from "@mui/material";
import {SyntheticEvent} from "react";
import SearchIcon from '@mui/icons-material/Search';
import "./SearchBar.css";

interface SearchBarProps {
    userTokenData: UserTokenData
    inputHandler: (e: SyntheticEvent) => void;
}

export function SearchBar(props: SearchBarProps) {

    return (
        <div className="search-panel">
            <div className="search">
                <Grid container>
                    <Grid item>
                        <Tooltip title={"Search not yet available. This would be a cypher-language console, see https://neo4j.com/developer/cypher/ for details."}>
                            <TextField
                                id="outlined-basic"
                                variant="outlined"
                                fullWidth
                                label="Search not yet available"
                                multiline
                                rows={2}
                                maxRows={4}
                                disabled={true}
                            />
                        </Tooltip>
                    </Grid>
                    <Grid item>
                        <IconButton disabled={true} type={"button"} onClick={props.inputHandler}>
                            <SearchIcon/>
                        </IconButton>
                    </Grid>
                </Grid>
            </div>
        </div>
    )
}
