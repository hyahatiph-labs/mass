import React, { ReactElement, useEffect } from 'react';
import axios from 'axios';
import { setGlobalState, useGlobalState } from '../../state';
import { PICO, PROXY } from '../../Config/constants';

// load balance once
let loaded = false;

const MoneroAccountComponent: React.FC = (): ReactElement => {
  const [gBalance] = useGlobalState('balance');
  const body = {
    jsonrpc: '2.0',
    id: '0',
    method: 'get_balance',
    params: { account_index: 0, address_indices: [0, 0] },
  };
  const loadXmrBalance = async (): Promise<void> => {
    await axios
      .post(`${PROXY}/monero/balance`, body)
      .then((res) => {
        const allBalances = res.data.result;
        setGlobalState('balance', {
          primaryAddress: '',
          walletBalance: allBalances.balance,
          unlockTime: allBalances.blocks_to_unlock,
          unlockedBalance: allBalances.unlocked_balance,
          subAddresses: [],
        });
        loaded = true;
      }).catch(() => { /* TODO: and snackbar for error handling */ });
  };

  useEffect(() => {
    if (!loaded) { loadXmrBalance(); }
  });

  const pendingBalance = gBalance.walletBalance - gBalance.unlockedBalance;
  const unlockTime = gBalance.unlockTime * 2;
  return (
    <div>
      <h1 color="#FF5722">
        {`${((gBalance.walletBalance - pendingBalance) / PICO).toFixed(6)} XMR`}
      </h1>
      <h4>{`*${(pendingBalance / PICO).toFixed(6)} (pending XMR)`}</h4>
      <h4>{`Time to unlock: ~${unlockTime} min.`}</h4>
    </div>
  );
};

export default MoneroAccountComponent;
