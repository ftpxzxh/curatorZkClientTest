package myZkClient;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

public class CuratorWatcherTest implements CuratorWatcher{

	public void process(WatchedEvent event) throws Exception {
		System.out.println("watcherEventPath:"+event.getPath());
		System.out.println("watcherEventType:"+event.getType());
	}

}
