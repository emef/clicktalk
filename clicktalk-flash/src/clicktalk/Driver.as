package clicktalk {

import clicktalk.streams.PublisherStream;

import com.codecatalyst.promise.Deferred;
import com.codecatalyst.promise.Promise;

import flash.events.AsyncErrorEvent;
import flash.events.IOErrorEvent;
import flash.events.NetStatusEvent;
import flash.events.SecurityErrorEvent;
import flash.media.Camera;
import flash.media.Video;
import flash.net.NetConnection;
import flash.net.NetStream;

import mx.controls.Alert;

public class Driver {
    private var main:Main;

    public function Driver(main:Main) {
        this.main = main;
    }

    public function init():void {
        /*main.startStopBtn.addEventListener("click", onClickStartStop);
        videoStream = createVideoStream("http://www.helpexamples.com/flash/video/cuepoints.flv");
        main.cam.addChild(videoFromCamera());
        main.player.addChild(videoFromStream(videoStream));*/

        var publisher: Promise = PublisherStream.create(
                "rtmp://localhost/clicktalk", "stream1", Camera.getCamera());

        publisher.then(function(stream: PublisherStream): void {
            Alert.show("Got publisher stream!");
            main.player.addChild(videoFromStream(stream.stream));
        })

    }

    /*
    private function netStatus(event: NetStatusEvent): void {
        switch (event.info.code) {
            case "NetConnection.Connect.Success":
                onConnect();
                break;

            case "NetConnection.Connect.Closed":
                trace("Disconnected from the RTMP server."); // debug trace..
                break;
        }
    }


    private function onConnect(): void {

        var camera: Camera = Camera.getCamera();
        camera.setMode(640, 480, 30, true);
        camera.setQuality(0, 80);

        publisherStream = new NetStream(publishNetConn);
        publisherStream.attachCamera(camera);
        publisherStream.publish("stream1");
        //this.oMetaData.onMetaData = eMetaDataReceived;
        //this.oNetStream.client = this.oMetaData;

        var nc = new NetConnection();
        nc.addEventListener(NetStatusEvent.NET_STATUS, function(evt: NetStatusEvent): void {
            var ns = new NetStream(nc);
            ns.bufferTime = 1;
            ns.play("stream1");
            main.player.addChild(videoFromStream(ns));
        });
        nc.connect("rtmp://localhost/clicktalk");
    }

    */


    /**
     * Create a Video DisplayObject from a net stream.
     *
     * @param stream - Netstream to create video from.
     * @return - Video object with input stream attached to it.
     */
    private function videoFromStream(stream: NetStream): Video {
        var video: Video = new Video();
        video.attachNetStream(stream);
        return video;
    }

    /**
     * Create a Video DisplayObject from the default camera.
     *
     * @return - Video object with default camera attached to it.
     */
    private function videoFromCamera(): Video {
        var video: Video = new Video();
        var cam: Camera = Camera.getCamera();
        cam.setMode(320, 240, 15);
        cam.setQuality(0, 80);
        video.attachCamera(cam);
        return video;
    }

    /**
     * Creates a net stream from a URI.
     *
     * @param uri - URI with protocol of stream.
     * @return - NetStream connected and playing from input uri.
     */
    private function createVideoStream(uri:String): NetStream {
        var connection:NetConnection = new NetConnection();
        connection.addEventListener(NetStatusEvent.NET_STATUS, doNetStatus);
        connection.addEventListener(IOErrorEvent.IO_ERROR, doIOError);
        connection.addEventListener(SecurityErrorEvent.SECURITY_ERROR, doSecurityError);
        connection.connect(null);

        var stream: NetStream = new NetStream(connection);
        stream.bufferTime = 1;
        stream.receiveAudio(true);
        stream.receiveVideo(true);
        stream.addEventListener(AsyncErrorEvent.ASYNC_ERROR, doAsyncError);
        stream.addEventListener(NetStatusEvent.NET_STATUS, doNetStatus);
        stream.addEventListener(IOErrorEvent.IO_ERROR, doIOError);
        stream.play(uri);
        stream.togglePause();

        return stream;
    }

    protected function doSecurityError(evt:SecurityErrorEvent):void
    {
        trace("AbstractStream.securityError:"+evt.text);
        // when this happens, you don't have security rights on the server containing the FLV file
        // a crossdomain.xml file would fix the problem easily
    }

    protected function doIOError(evt:IOErrorEvent):void
    {
        trace("AbstractScreem.ioError:"+evt.text);
        // there was a connection drop, a loss of internet connection, or something else wrong. 404 error too.
    }

    protected function doAsyncError(evt:AsyncErrorEvent)
    {
        trace("AsyncError:"+evt.text);
        // this is more related to streaming server from my experience, but you never know
    }

    protected function doNetStatus(evt:NetStatusEvent):void
    {
        trace(evt.info.code);
        // this will eventually let us know what is going on.. is the stream loading, empty, full, stopped?
    }
}
}
