import React, {Component} from 'react';
import {BrowserRouter as Router, Link, Route} from 'react-router-dom';
import './App.css';
import Products from './products/Products'
import {ProductDetails} from "./products/ProductDetails";

class App extends Component {

    componentDidMount() {
        const script = document.createElement("script");

        script.src = "https://accounts.google.com/gsi/client";
        script.async = true;

        document.body.appendChild(script);
    }

    render() {
        return <div>
            <div>
                <div id="g_id_onload"
                     data-client_id="183614945179-dliqa6853fpsvs8k64oa2ubb632s5vha.apps.googleusercontent.com"
                     data-login_uri="http://localhost:8080/auth"
                     data-auto_prompt="false">
                </div>
                <div className="g_id_signin"
                     data-type="standard"
                     data-size="large"
                     data-theme="outline"
                     data-text="sign_in_with"
                     data-shape="rectangular"
                     data-logo_alignment="left">
                </div>
            </div>
            <Router>
                <Route path="/products" component={Products}/>
                <Route path="/details/:id" component={ProductDetails}/>
            </Router>
        </div>

    }
}

export default App;