import {Button} from "@mui/material";
import React from "react";
import {useHistory} from 'react-router-dom'


export function ProductsDetailsButton({selected}) {
    const history = useHistory();

    function handleClick() {
        history.push("/details/" + selected[0]);
    }

    return <div>
        <Button type="submit" variant="contained" onClick={handleClick}>Details</Button>
    </div>
}