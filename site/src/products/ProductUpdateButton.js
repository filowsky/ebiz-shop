import {Button} from "@mui/material";
import React, {useCallback} from "react";
import sendRequest from "../requests";

export function ProductUpdateButton({products, onProductsChange, selected, onSelectedChange}) {

    const handleProductsChange = useCallback(event => {
        onProductsChange(products.concat([event]))
    }, [onProductsChange, products])

    const updateProduct = (event) => {
        console.log(selected[0])
        onSelectedChange(null)
        sendRequest('https://ebiz-shop-backend-brqleqljrq-lm.a.run.app/products/' + "id", {
            name: "name",
            description: "description",
            category: "category"
        }, 'POST').then(data => {
            handleProductsChange(data)
        });
        event.preventDefault()
    }

    return <div>
        <Button type="submit" variant="contained" onClick={updateProduct}>Update</Button>
    </div>
}