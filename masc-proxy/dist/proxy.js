"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
// build cors proxy for monero
const app = (0, express_1.default)();
app.use(express_1.default.json());
const corsOptions = {
    origin: "*",
    method: ["GET", "POST"],
    optionsSuccessStatus: 200
};
app.use((0, cors_1.default)(corsOptions));
// API routes
const monero = require("./routes/monero");
// Set route paths
app.use("/proxy/monero", monero);
// Server port
const port = process.env.PORT || 5000;
// Start server
app.listen(port, () => console.log(`MASC proxy server running on port ${port}`));
//# sourceMappingURL=proxy.js.map