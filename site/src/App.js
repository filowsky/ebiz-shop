import React, {Component} from 'react';
import {BrowserRouter as Router, Route} from 'react-router-dom';
import './App.css';
import Products from './products/Products'
import {ProductDetails} from "./products/ProductDetails";
import {Home} from "./products/Home";

class App extends Component {
    render() {
        return <Router>
            <Route path="/" component={Home}/>
            <Route path="/products" component={Products}/>
            <Route path="/details/:id" component={ProductDetails}/>
        </Router>
    }
}

export default App;