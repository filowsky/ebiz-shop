import sendRequest from "../sendRequest";
import React, {Component} from "react";
import {Button, Typography} from "@mui/material";

export class ProductDetails extends Component {
    componentDidMount() {
        this.getProduct(this.props.match.params.id);
    }

    constructor() {
        super();
        this.state = {
            id: '',
            name: '',
            category: '',
            description: ''
        };
        this.putProduct = this.putProduct.bind(this);

    }

    async getProduct(productId) {
        let result = await sendRequest('https://ebiz-shop-backend-brqleqljrq-lm.a.run.app/products/' + productId, null)
        this.setState({
            id: result.id,
            name: result.name,
            category: result.category,
            description: result.description
        })
    }

    async putProduct(event) {
        const id = this.state.id
        const name = this.state.name
        const description = this.state.description
        const category = this.state.category
        await sendRequest('https://ebiz-shop-backend-brqleqljrq-lm.a.run.app/products' + '/' + id, {
            name: name,
            description: description,
            category: category
        }, 'PUT')
        event.preventDefault()
    }

    render() {
        return (
            <ul>
                <li>
                    <b>ID: </b>
                    <Typography><b>{this.state.id}</b></Typography>
                </li>
                <li>
                    Name:
                    <input type="text" readOnly={false} defaultValue={this.state.name} onChange={(e) => {
                        this.setState({name: e.target.value})
                        console.log(this.state.name)
                    }}/>
                </li>
                <li>
                    Description:
                    <input type="text" readOnly={false} defaultValue={this.state.description} onChange={(e) => {
                        this.setState({description: e.target.value})
                        console.log(this.state.description)
                    }}/>
                </li>
                <li>
                    Category:
                    <input type="text" readOnly={false} defaultValue={this.state.category} onChange={(e) => {
                        this.setState({category: e.target.value})
                        console.log(this.state.category)
                    }}/>
                </li>
                <li>
                    <Button type="submit" variant="contained" onClick={this.putProduct}>Submit</Button>
                </li>
            </ul>
        )
    }
}