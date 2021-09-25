const express = require("express");
const router = express.Router();
import axios from 'axios';

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
      .post('http://localhost:38083/json_rpc', req.body)
      .then((balance) => {
        console.log(`DEBUG: ${JSON.stringify(balance.data)}`);
        res.json( balance.data )
    }).catch((e) => {
        console.error(e);
        res.status(400).json({error:"get_balance failed"});
    })
});

module.exports = router;