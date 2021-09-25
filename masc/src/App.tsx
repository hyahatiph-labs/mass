import React, { ReactElement } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Drawer from '@material-ui/core/Drawer';
import AppBar from '@material-ui/core/AppBar';
import CssBaseline from '@material-ui/core/CssBaseline';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import Typography from '@material-ui/core/Typography';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import SettingsIcon from '@material-ui/icons/Settings';
import ExtensionIcon from '@material-ui/icons/Extension';
import FlashOnIcon from '@material-ui/icons/FlashOn';
import AccountBalanceWalletIcon from '@material-ui/icons/AccountBalanceWallet';
import ListItemText from '@material-ui/core/ListItemText';
import ImportExportIcon from '@material-ui/icons/ImportExport';
import axios from 'axios';
import logo from './Assets/logo.png';
import { setGlobalState, useGlobalState } from './state';
import { PICO, PROXY } from './Config/constants';

// TODO: refactor to Main Component

const drawerWidth = 240;

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
  },
  appBar: {
    backgroundColor: '#FF9A7A',
    zIndex: theme.zIndex.drawer + 1,
  },
  drawer: {
    width: drawerWidth,
    flexShrink: 0,
  },
  drawerPaper: {
    width: drawerWidth,
  },
  drawerContainer: {
    overflow: 'auto',
    color: '#FF5722',
  },
  content: {
    fontFamily: 'sagona',
    flexGrow: 1,
    padding: theme.spacing(10),
    color: '#FF5722',
  },
}));

const App: React.FC = (): ReactElement => {
  const [gBalance] = useGlobalState('balance');
  const classes = useStyles();
  const body = {
    jsonrpc: '2.0',
    id: '0',
    method: 'get_balance',
    params: { account_index: 0, address_indices: [0, 0] },
  };
  const loadBalance = async (): Promise<void> => {
    await axios
      .post(`${PROXY}/monero/balance`, body)
      .then((res) => {
        setGlobalState('balance', {
          primaryAddress: '',
          walletBalance: res.data.result.balance,
          unlockTime: 0,
          unlockedBalance: 0,
          subAddresses: [],
        });
      }).catch(() => { /* TODO: and snackbar for error handling */ });
  };
  loadBalance();
  // TODO: implement pending balance, unlock time, etc.
  const pendingBalance = 10.78;
  const unlockTime = 10;
  return (
    <div className={classes.root}>
      <CssBaseline />
      <AppBar position="fixed" className={classes.appBar}>
        <Toolbar>
          <img src={logo} alt="monero logo" width={50} />
          <Typography variant="h6" noWrap>
            MASC v0.1.0
          </Typography>
        </Toolbar>
      </AppBar>
      <Drawer
        className={classes.drawer}
        variant="permanent"
        classes={{
          paper: classes.drawerPaper,
        }}
      >
        <Toolbar />
        <div className={classes.drawerContainer}>
          <List>
            <ListItem button key="Wallet">
              <ListItemIcon>
                <AccountBalanceWalletIcon />
              </ListItemIcon>
              <ListItemText primary="Wallet" />
            </ListItem>
            <ListItem button key="Transactions">
              <ListItemIcon>
                <ImportExportIcon />
              </ListItemIcon>
              <ListItemText primary="Transactions" />
            </ListItem>
            <ListItem button key="MASS">
              <ListItemIcon>
                <ExtensionIcon />
              </ListItemIcon>
              <ListItemText primary="MASS" />
            </ListItem>
            <ListItem button key="LN">
              <ListItemIcon>
                <FlashOnIcon />
              </ListItemIcon>
              <ListItemText primary="LN" />
            </ListItem>
            <ListItem button key="Settings">
              <ListItemIcon>
                <SettingsIcon />
              </ListItemIcon>
              <ListItemText primary="Settings" />
            </ListItem>
          </List>
        </div>
      </Drawer>
      <main className={classes.content}>
        <Toolbar />
        <h1 color="#FF5722">
          {`${(gBalance.walletBalance / PICO).toFixed(4)} XMR`}
        </h1>
        <h4>{`*${pendingBalance.toFixed(2)} XMR`}</h4>
        <h4>{`Time to unlock: ~${unlockTime} min.`}</h4>
      </main>
    </div>
  );
};

export default App;
