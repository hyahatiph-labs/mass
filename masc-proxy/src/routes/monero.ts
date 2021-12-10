const express = require("express");
const router = express.Router();
import axios from 'axios';
import { spawn } from 'child_process';
import { keys } from '../utils';
import { CONFIG, MASC_ENV } from '../config';

const isMainnet: boolean = process.env.MASC_PROXY_ENV === MASC_ENV.MAINNET;
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

// TODO: generate wallet from phrase, generate one if not existing

// @route GET /proxy/monero/wallet/seed
// @desc generate keys, address and seed
// @access Public
// @status Working
router.get("/wallet/seed", (req:any, res:any) => {
    const pythonProcess = spawn('python',['src/xmr.py','seed']);
    const allKeys: keys = {
        address: "",
        privateSpendKey: "",
        privateViewKey: "",
        publicSpendKey: "",
        publicViewKey: "",
        phrase: ""
    }
    pythonProcess.stdout.on('data', (data) => {
        const json = JSON.parse(data)
        allKeys.address = json.address
        allKeys.privateSpendKey = json.sk_private
        allKeys.privateViewKey = json.vk_private
        allKeys.publicSpendKey = json.sk_public
        allKeys.publicViewKey = json.vk_public
        allKeys.phrase = json.phrase
        res.json(allKeys);
    });
});

module.exports = router;