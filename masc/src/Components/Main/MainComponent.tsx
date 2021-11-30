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
import logo from '../../Assets/logo.png';
import MoneroAccountComponent from '../Monero/MoneroAccountComponent';

const drawerWidth = 240;
const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
    backgroundColor: '#212D36',
  },
  appBar: {
    backgroundColor: '#FF5722',
    zIndex: theme.zIndex.drawer + 1,
  },
  drawer: {
    width: drawerWidth,
    flexShrink: 0,
  },
  drawerPaper: {
    width: drawerWidth,
    backgroundColor: '#212D36',
  },
  drawerContainer: {
    overflow: 'auto',
    backgroundColor: '#212D36',
    color: '#FF5722',
  },
  content: {
    fontFamily: 'sagona',
    flexGrow: 1,
    padding: theme.spacing(10),
    color: '#FF5722',
    backgroundColor: '#FFF',
  },
}));

const MainComponent: React.FC = (): ReactElement => {
  const classes = useStyles();

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
            <ListItem button key="Swap">
              <ListItemIcon>
                <ExtensionIcon />
              </ListItemIcon>
              <ListItemText primary="Swap" />
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
        <MoneroAccountComponent />
      </main>
    </div>
  );
};

export default MainComponent;
