import React, {useCallback, useState} from "react";
import {Button, TextField} from "@mui/material";
import sendRequest from "../requests";

export function ProductAdd({products, onProductsChange}) {

    const [newProductName, setNewProductName] = useState('')
    const [newProductDescription, setNewProductDescription] = useState('')
    const [newProductCategory, setNewCategory] = useState('')

    const handleProductsChange = useCallback(event => {
        onProductsChange(products.concat([event]))
    }, [onProductsChange, products])

    const postProduct = (event) => {
        sendRequest('http://localhost:8080/products', {
            name: newProductName,
            description: newProductDescription,
            category: newProductCategory
        }, 'POST').then(data => {
            handleProductsChange(data)
        });
        event.preventDefault()
    }


    return <div>
        <TextField id="outlined-basic" label="Name" variant="outlined" value={newProductName}
                   onChange={e => setNewProductName(e.target.value)}/>
        <TextField id="outlined-basic" label="Description" variant="outlined"
                   value={newProductDescription} onChange={e => setNewProductDescription(e.target.value)}/>
        <TextField id="outlined-basic" label="Category" variant="outlined"
                   onChange={e => setNewCategory(e.target.value)}/>
        <Button type="submit" variant="contained" onClick={postProduct}>Submit</Button>
    </div>

}