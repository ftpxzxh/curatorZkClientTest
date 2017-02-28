package myZkClient;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;

public class DeleteBackgroundCallback implements BackgroundCallback{

	public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
		System.out.println(event.getPath()+",data:"+event.getData());
		System.out.println("eventType:"+event.getType());
		System.out.println("resultCode:"+event.getResultCode());
	}

}
