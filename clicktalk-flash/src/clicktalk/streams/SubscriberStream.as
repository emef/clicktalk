package clicktalk.streams {
import clicktalk.events.SubscriberEvent;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.NetStatusEvent;
import flash.net.NetConnection;
import flash.net.NetStream;

public class SubscriberStream extends EventDispatcher {
    private var netConn: NetConnection;
    private var netStream: NetStream;
    private var streamId: String;
    private var bufferTime: int;

    public function SubscriberStream(rtmpUri: String, streamId: String, bufferTime:int) {
        this.streamId = streamId;
        this.bufferTime = bufferTime;
        this.netConn = new NetConnection();
        this.netConn.addEventListener(NetStatusEvent.NET_STATUS, onStatus);
        this.netConn.connect(rtmpUri);
    }

    private function onStatus(event: NetStatusEvent): void {
        if (event.info.code != "NetConnection.Connect.Success") {
            this.dispatchEvent(new Event(SubscriberEvent.CONNECTION_ERROR));
            return;
        }

        netStream = new NetStream(netConn);
        netStream.bufferTime = bufferTime;
        netStream.play(streamId);

        this.dispatchEvent(new Event(SubscriberEvent.STREAM_SUBSCRIBED));
    }
}
}
