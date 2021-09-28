import {Button} from "@mui/material";
import React, {useCallback} from "react";
import sendRequest from "../requests";

export function ProductDelete({products, onProductsChange, selected, onSelectedChange}) {
    const handleProductsChange = useCallback(removed => {
        onProductsChange(removed)
        onSelectedChange(null)
    }, [onProductsChange, onSelectedChange])

    const deleteProduct = (event) => {
        const removed = products.filter((prod) => prod.id !== selected[0])
        sendRequest('http://localhost:8080/products/' + selected[0], null, 'DELETE')
            .then(() => {
                handleProductsChange(removed)
            });
        event.preventDefault()
    }

    return <div>
        <Button type="submit" variant="contained" onClick={e => deleteProduct(e)}>Delete</Button>
    </div>
}