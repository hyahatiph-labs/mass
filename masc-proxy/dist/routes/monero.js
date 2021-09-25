"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express = require("express");
const router = express.Router();
const axios_1 = __importDefault(require("axios"));
// @route GET /proxy/monero/test
// @desc Tests monero rpc route
// @access Public
// @status Working
router.get("/test", (_, res) => res.json({
    msg: "monero api proxy is up!",
}));
// @route POST /proxy/monero/balance
// @desc get_balance rpc call
// @access Public
// @status Working
router.post("/balance", (req, res) => {
    console.log(req.body);
    axios_1.default
        .post('http://localhost:38083/json_rpc', req.body)
        .then((balance) => {
        console.log(`DEBUG: ${JSON.stringify(balance.data)}`);
        res.json(balance.data);
    }).catch((e) => {
        console.error(e);
        res.status(400).json({ error: "get_balance failed" });
    });
});
module.exports = router;
//# sourceMappingURL=monero.js.map