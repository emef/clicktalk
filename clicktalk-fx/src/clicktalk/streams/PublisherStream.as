/**
 * Created by mforbes on 7/6/15.
 */
package clicktalk.streams {
import clicktalk.events.PublisherEvent;

import com.codecatalyst.promise.Deferred;
import com.codecatalyst.promise.Promise;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.NetStatusEvent;
import flash.media.Camera;
import flash.net.NetConnection;
import flash.net.NetStream;

public class PublisherStream extends EventDispatcher {
    private var netStream: NetStream;
    private var netConn: NetConnection;
    private var streamId: String;
    private var camera: Camera;

    public static function create(rtmpUri: String, streamId: String, camera: Camera): Promise  {
        var deferred: Deferred = new Deferred();
        var stream: PublisherStream = new PublisherStream(rtmpUri, streamId, camera);

        stream.addEventListener(PublisherEvent.CONNECTION_ERROR, function(event: Event): void {
            deferred.reject("Could not connect to RTMP server");
        });

        stream.addEventListener(PublisherEvent.STREAM_PUBLISHED, function(event: Event): void {
            deferred.resolve(stream);
        });

        return deferred.promise;
    }

    function PublisherStream(rtmpUri: String, streamId: String, camera: Camera) {
        this.streamId = streamId;
        this.camera = camera;
        this.netConn = new NetConnection();
        this.netConn.addEventListener(NetStatusEvent.NET_STATUS, onStatus);
        this.netConn.connect(rtmpUri);
    }

    private function onStatus(event: NetStatusEvent): void {
        if (event.info.code != "NetConnection.Connect.Success") {
            this.dispatchEvent(new Event(PublisherEvent.CONNECTION_ERROR));
            return;
        }

        netStream = new NetStream(netConn);
        netStream.attachCamera(camera);
        netStream.publish(streamId);

        this.dispatchEvent(new Event(PublisherEvent.STREAM_PUBLISHED))
    }

    public function get stream(): NetStream {
        return netStream;
    }

    public function get connection(): NetConnection {
        return netConn;
    }
}
}
