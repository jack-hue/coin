package com.coin.neo.Network;

import com.coin.neo.UInt256;
import com.coin.neo.Core.Signable;
import com.coin.neo.Cryptography.Digest;

public abstract class Inventory implements Signable {
    //[NonSerialized]
    private UInt256 _hash = null;
    
    public UInt256 hash() {
        if (_hash == null) {
			_hash = new UInt256(Digest.hash256(getHashData()));
        }
        return _hash;
    }

    public abstract InventoryType inventoryType();

    public abstract boolean verify();
}
