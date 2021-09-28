import Cookies from "js-cookie";

export default async function sendRequest(url, data, method='GET') {
    const result = await fetch(url, {
        method,
        headers: {
            'Accept': 'application/json',
            'Content-type': 'application/json',
            'Authorization': Cookies.get('shop_auth')
        },
        mode: 'cors',
        credentials: 'include',
        body: data ? JSON.stringify(data) : undefined,
    })
    return result.json();
}