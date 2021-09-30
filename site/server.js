const express = require('express')
const cors = require('cors');
const app = express()
const port = 5000

app.use(cors())

app.get('/auth', (req, res) => {
    res.send('token')
})

app.listen(port, () => {
    console.log(`Example app listening at http://localhost:${port}`)
})