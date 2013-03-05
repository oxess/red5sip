package org.red5.sip.app;

import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.red5.codecs.SIPCodec;
import org.red5.sip.app.SIPVideoConverter.RTMPPacketInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.net.RtpPacket;
import local.net.RtpSocket;

public class RTPStreamVideoReceiver extends Thread {

	protected static Logger log = LoggerFactory.getLogger(RTPStreamVideoReceiver.class);
	protected RtpSocket rtpSocket;
	protected IMediaReceiver mediaReceiver;
	protected SIPCodec codec;
	private boolean running;
	private ConverterThread converterThread;
	
	public RTPStreamVideoReceiver(IMediaReceiver mediaReceiver, DatagramSocket socket, SIPCodec codec) {
		this.mediaReceiver = mediaReceiver;
		rtpSocket = new RtpSocket(socket);
		this.codec = codec;
		converterThread = new ConverterThread();
	}

	@Override
	public void interrupt() {
		running = false;
		converterThread.interrupt();
	}

	@Override
	public void run() {
		running = true;
		converterThread.start();
		try {
			while(running) {
				byte[] sourceBuffer = new byte[codec.getIncomingDecodedFrameSize()];
				RtpPacket rtpPacket = new RtpPacket(sourceBuffer, 0);
				rtpSocket.receive(rtpPacket);
				converterThread.addPacket(rtpPacket);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		rtpSocket.close();
	}
	
	private class ConverterThread extends Thread {

		private final Queue<RtpPacket> packetQueue;
		private boolean running;
		private SIPVideoConverter converter;
		
		public ConverterThread() {
			packetQueue = new ConcurrentLinkedQueue<RtpPacket>();
			converter = new SIPVideoConverter();
		}
		
		public void addPacket(RtpPacket packet) {
			if (isInterrupted()) return;
			packetQueue.add(packet);
		}
		
		@Override
		public void run() {
			running = true;
			while(running) {
				try {
					RtpPacket packet = packetQueue.poll();
					if (packet != null) {
						for (RTMPPacketInfo packetInfo: converter.rtp2rtmp(packet, codec)) {
							mediaReceiver.pushVideo(packetInfo.data, packetInfo.ts);
						}
					}
					if (packetQueue.size() == 0) {
						Thread.sleep(50);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}

		@Override
		public void interrupt() {
			running = false;
			packetQueue.clear();
		}
		
	}
	
}
