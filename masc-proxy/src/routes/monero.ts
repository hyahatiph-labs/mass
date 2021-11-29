const express = require("express");
const router = express.Router();
import axios from 'axios';
import { CONFIG, MASC_ENV } from '../config';

const isMainnet: boolean = process.env.MASC_PROXY_ENV === MASC_ENV.MAINNET
const HOST = isMainnet ? CONFIG.MAINNET_HOST : CONFIG.STAGENET_HOST;
const PORT = isMainnet ? CONFIG.MAINNET_PORT : CONFIG.STAGENET_PORT;

// @route GET /proxy/monero/test
// @desc Tests monero rpc route
// @access Public
// @status Working
router.get("/test", (_:any, res:any) =>
  res.json({
    msg: "monero api proxy is up!",
  })
);

// @route POST /proxy/monero/balance
// @desc get_balance rpc call
// @access Public
// @status Working
router.post("/balance", (req:any, res:any) => {
  console.log(req.body);
    axios
      .post(`http://${HOST}:${PORT}/json_rpc`, req.body)
      .then((balance) => {
        console.log(`DEBUG: ${JSON.stringify(balance.data)}`);
        res.json( balance.data )
    }).catch((e) => {
        console.error(e);
        res.status(400).json({error:"get_balance failed"});
    })
});

module.exports = router;