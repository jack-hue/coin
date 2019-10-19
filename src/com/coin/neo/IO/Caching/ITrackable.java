package com.coin.neo.IO.Caching;

public interface ITrackable<TKey> {
    TKey key();
    TrackState getTrackState();
    void setTrackState(TrackState state);
}
