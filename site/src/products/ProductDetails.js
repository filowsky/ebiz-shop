import sendRequest from "../requests";
import {Component} from "react";
import {Box} from "@mui/material";

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
    }

    async getProduct(productId) {
        let result = await sendRequest('http://localhost:8080/products/' + productId, null)
        this.setState({
            id: result.id,
            name: result.name,
            category: result.category,
            description: result.description
        })
    }

    render() {
        return (
            <ul>
                <li>{this.state.id}</li>
                <li>{this.state.name}</li>
                <li>{this.state.description}</li>
                <li>{this.state.category}</li>
            </ul>
        )
    }
}