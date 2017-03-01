package lock.exclusive;

import java.util.concurrent.CountDownLatch;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

public class MyLockUtil {
	private static final String connectString = "pay.zk.01:2181,pay.zk.02:2182,pay.zk.03:2183";
	private CuratorFramework curatorZkClient = null;
	private CountDownLatch latch = new CountDownLatch(1);
	
	public MyLockUtil() {
		super();
		init();
	}

	public void init(){
		if(null==curatorZkClient){
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(5000, 3);	
			curatorZkClient = CuratorFrameworkFactory.builder().connectString(connectString)
					.sessionTimeoutMs(10000).connectionTimeoutMs(10000)
					.namespace("lockSpace").retryPolicy(retryPolicy ).build();
			curatorZkClient.start();
			if(null==curatorZkClient){
				System.out.println("����zkʧ�ܣ�");
				return;
			}
		}
		
		String path = "/exclusiveLock";
		try {
			if(null==curatorZkClient.checkExists().forPath(path)){
				System.out.println("����/exclusiveLock");
				curatorZkClient.create().withMode(CreateMode.PERSISTENT).withACL(Ids.OPEN_ACL_UNSAFE).forPath(path);
			}
			
			System.out.println("��ӶԽڵ�/exclusiveLock�ļ���");
			PathChildrenCache pathCache = new PathChildrenCache(curatorZkClient, path, true);
			pathCache.start(StartMode.NORMAL);
			pathCache.getListenable().addListener(new PathChildrenCacheListener() {
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//					System.out.println("type:" + event.getType());
//					System.out.println("getPath:" + event.getData().getPath());
//					System.out.println("getData:" + event.getData().getData());
					if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
						System.out.println(Thread.currentThread().getName()+"�����������ͷ���");
						latch.countDown();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void  getExclusiveLock(){
		while(true){
			String path = "/exclusiveLock/lock1";
			try {
				curatorZkClient.create().withMode(CreateMode.EPHEMERAL).withACL(Ids.OPEN_ACL_UNSAFE).forPath(path);
				System.out.println(Thread.currentThread().getName()+"��ȡ���ɹ�");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				try {
					System.out.println(Thread.currentThread().getName()+"��ȡ��ʧ�ܣ��ȴ����ͷ�");
					if(latch.getCount()==0){
						latch = new CountDownLatch(1);
					}
					latch.await();
					System.out.println(Thread.currentThread().getName()+"���»�ȡ��");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void unLockForExclusive(){
		String path = "/exclusiveLock/lock1";
		try {
			if(null!=curatorZkClient.checkExists().forPath(path)){
				curatorZkClient.delete().forPath(path);
				System.out.println(Thread.currentThread().getName()+"�����ͷ���");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
