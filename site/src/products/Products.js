import React, {useEffect, useState} from 'react';
import {Grid} from "@mui/material";

import {ProductAdd} from '../products/ProductAdd'
import sendRequest from "../sendRequest";
import {ProductDelete} from "./ProductDelete";
import {ProductsTable} from "./ProductsTable";
import {ProductsDetailsButton} from "./ProductsDetailsButton";
import {ProductUpdateButton} from "./ProductUpdateButton";

function Products() {

    const [products, setProducts] = useState([])
    const [selected, setSelected] = useState(null)

    const getProducts = async () => {
        const url = "https://ebiz-shop-backend-brqleqljrq-lm.a.run.app/products";
        const data = await sendRequest(url, null)
        setProducts(data)
    }

    useEffect(() => {
        getProducts()
    }, []);

    return <Grid sx={{ flexGrow: 1 }} >
        <Grid sx={{ flexGrow: 1 }} >
            <ProductAdd products={products} onProductsChange={setProducts}/>
            <ProductDelete products={products} onProductsChange={setProducts} selected={selected}
                           onSelectedChange={setSelected}/>
            <ProductsDetailsButton selected={selected}/>
        </Grid>
        <Grid sx={{ flexGrow: 2 }} height = {1200}>
            <ProductsTable products={products} onSelectedChange={setSelected}/>
        </Grid>
    </Grid>
}

export default Products;