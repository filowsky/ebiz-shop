import React, {useEffect, useState} from 'react';
import {Box} from "@mui/material";
import {ProductAdd} from '../products/ProductAdd'
import sendRequest from "../requests";
import {ProductDelete} from "./ProductDelete";
import {ProductsTable} from "./ProductsTable";
import {ProductsDetailsButton} from "./ProductsDetailsButton";
import {ProductUpdateButton} from "./ProductUpdateButton";

function Products() {

    const [products, setProducts] = useState([])
    const [selected, setSelected] = useState(null)

    const getProducts = async () => {
        const url = "http://localhost:8080/products";
        const data = await sendRequest(url, null)
        setProducts(data)
    }

    useEffect(() => {
        getProducts()
    }, []);

    return <Box
        component="form"
        sx={{
            '& .MuiTextField-root': {m: 1, width: '25ch'},
        }}
        noValidate
        autoComplete="off"
    >
        <div className="products">
            <ProductAdd products={products} onProductsChange={setProducts}/>
            <ProductDelete products={products} onProductsChange={setProducts} selected={selected} onSelectedChange={setSelected}/>
            <ProductsDetailsButton selected={selected}/>
            <ProductUpdateButton products={products} onProductsChange={setProducts} selected={selected} onSelectedChange={setSelected}/>
            <ProductsTable products={products} onSelectedChange={setSelected}/>
        </div>
    </Box>
}

export default Products;