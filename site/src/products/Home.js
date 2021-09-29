import {Button, Grid, Paper, Typography} from "@mui/material";
import {Link, useHistory} from "react-router-dom";
import React, {useEffect} from "react";
import Cookies from "js-cookie";

export function Home() {
    const history = useHistory();

    const isLoggedIn = Cookies.get("shop_auth");
    let button;
    if (isLoggedIn !== undefined) {
        button = <Grid><Button onClick={handleClick}>Sign out</Button></Grid>
    } else {
        button = <Grid>
            <div id="g_id_onload"
                 data-client_id="183614945179-dliqa6853fpsvs8k64oa2ubb632s5vha.apps.googleusercontent.com"
                 data-login_uri="https://ebiz-shop-backend-brqleqljrq-lm.a.run.app/auth"
                 data-auto_prompt="false">
            </div>
            <div className="g_id_signin"
                 data-type="standard"
                 data-size="large"
                 data-theme="outline"
                 data-text="sign_in_with"
                 data-shape="rectangular"
                 data-logo_alignment="center"
            />
        </Grid>
    }

    function handleClick() {
        Cookies.remove("shop_auth");
        window.location.reload();
    }

    useEffect(() => {
        const script = document.createElement("script");

        script.src = "https://accounts.google.com/gsi/client";
        script.async = true;

        document.body.appendChild(script);
    }, []);

    return <Grid sx={{ flexGrow: 1 }} container spacing={10}>
        <Grid item xs={12} color={"blue"}>
            <Paper sx={{ p: 2 }} elevation={3}>
                <Grid container>
                    <Grid item sx={{ flexGrow: 1 }} container spacing={2} >
                        {button}
                        {/*<Grid><Typography fontStyle={"aria"}>Ebiz shop</Typography></Grid>*/}
                    </Grid>
                </Grid>
            </Paper>
        </Grid>
        <Grid item xs={12} >
            <Grid container justifyContent="center" spacing={5}>
                <Grid item>
                    <Paper sx={{ height: 100, width: 600 }} elevation={3}>
                        <Link to="/products">
                            <button type="button">
                                Products
                            </button>
                        </Link>
                    </Paper>
                </Grid>
                <Grid item>
                    <Paper sx={{ height: 100, width: 600 }} elevation={3}>
                        <Link to="/">
                            <button type="button">
                                Cart
                            </button>
                        </Link>
                    </Paper>
                </Grid>
            </Grid>
        </Grid>
    </Grid>
}