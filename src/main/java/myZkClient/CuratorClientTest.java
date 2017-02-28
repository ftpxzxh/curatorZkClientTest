package myZkClient;

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuratorClientTest {
	private CuratorFramework curatorZkClient = null;
	
	private static final String connectString = "pay.zk.01:2181,pay.zk.02:2182,pay.zk.03:2183";
	
	//静态工厂的方式返回curator客户端
//	@Before
//	public void getCuratorClient1() {
//		RetryPolicy retryPolicy = new RetryOneTime(5000);//连接不上时，间隔5s中后再重试一次，如果还是连不上就不管了
//		curatorZkClient = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
//		curatorZkClient.start();
//	}
	
	//Fluent风格的方式返回curator客户端
//	@Before
//	public void getCuratorClient2() {
//		RetryPolicy retryPolicy = new RetryNTimes(5, 5000);//连接不上时，每间隔5s重试一次，共重试5次
//		int sessionTimeoutMs = 60000;//会话超时时间，默认是60s
//		int connectionTimeoutMs = 15000;//连接超时时间，默认是15s
//		curatorZkClient = CuratorFrameworkFactory.newClient(connectString , sessionTimeoutMs , connectionTimeoutMs , retryPolicy);
//		curatorZkClient.start();
//	}
	//Fluent风格的方式返回curator客户端
	@Before
	public void getCuratorClient3() {
//		第一次重试的间隔是1s，按照指数倍增的重试间隔进行重连，重连最大次数是3；
//		例如第一次连不上，间隔1s后重连；还是连不上，再间隔2s进行重连；还是连不上，再间隔4s后重连；还是连接不上，这个时候已经重试了3次了，就不再重试了
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		curatorZkClient = CuratorFrameworkFactory.builder()
				.connectString(connectString)
				.sessionTimeoutMs(10000)		//会话超时时间，单位为毫秒，默认60000ms
				.connectionTimeoutMs(10000)     //连接创建超时时间，单位为毫秒，默认是15000ms
				.retryPolicy(retryPolicy)		//重试策略
				.namespace("curatorClientTest") //设置该客户端的根目录
				.build();
		curatorZkClient.start();
	}
	
	@After
	public void closeCurator(){
		if(null!=curatorZkClient){
			curatorZkClient.close();
		}
	}
	
	//创建一个普通的节点，/curatorClientTest/aa并把它的值设置为aa
	@Test
	public void createNode1() throws Exception{
		String path = "/watcherTest";//path必须以/开头，否则会报错
		String data = "ww2";
		curatorZkClient.create().forPath(path , data.getBytes());	
	}
	
	//递归创建/curatorClientTest/bb1/bb2节点，并把bb2的节点值设置为bb2
	@Test
	public void createNode2() throws Exception{
		String path = "/watcherTest/bb1";
		String data = "bb1";
		curatorZkClient.create().creatingParentsIfNeeded().forPath(path , data.getBytes());	
	}
	
	//创建一个持久化节点withMode(CreateMode.PERSISTENT)
	@Test
	public void createNode3() throws Exception{
		String path = "/cc";
		String data = "cc";
		curatorZkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path , data.getBytes());	
	}
	//创建一个临时节点，测试时可以注释掉closeCurator方法，等到会话超时时间后消失
	@Test
	public void createNode4() throws Exception{
		String path = "/dd";
		String data = "dd";
		curatorZkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path , data.getBytes());	
	}
	//创建一个临时节点，测试时可以注释掉closeCurator方法，等到会话超时时间后消失
	@Test
	public void createNode5() throws Exception{
		String path = "/ephemeral_seq";
		String data = "ephemeral_seq_data";
		curatorZkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path , data.getBytes());	
	}
	//创建一个序列号持久化节点
	@Test
	public void createNode6() throws Exception{
		String path = "/seq";
		String data = "persistent_seq";
		curatorZkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path , data.getBytes());	
	}
	
	//读取数据
	@Test
	public void get() throws Exception{
		String path = "/aa";
		Stat stat = new Stat();
		byte[] data = curatorZkClient.getData()
				.storingStatIn(stat)//把服务器端获取的状态数据存储到stat对象
				.forPath(path);//节点路径
		System.out.println(new String(data));
		System.out.println(stat.toString());
	}
	
	//更新节点的数据
	@Test
	public void update1() throws Exception{
		String path = "/watcherTest/bb1";
		String data = "bb1";
		curatorZkClient.setData()
//		.withVersion(0)//特定版本号，可不加；可以加上读取节点时的版本号，实现一种乐观锁的效果
		.forPath(path, data.getBytes());
	}
	//更新节点的数据，值会变成192.168.5.1，dataVersion版本号会加1
	@Test
	public void update2() throws Exception{
		String path = "/watcherTest/aa";
		String data = "aa2";
		curatorZkClient.setData()
		.forPath(path);
	}
	
	//删除一个节点
	@Test
	public void del1() throws Exception{
//		String path = "/curatorTest2/bb";
		String path = "/watcherTest/aa";
		curatorZkClient.delete().forPath(path);
	}
	
	//递归删除所有子节点deletingChildrenIfNeeded()
	@Test
	public void del2() throws Exception{
		String path = "/curatorTest2";
		curatorZkClient.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
	}
	
	//带版本号删除,版本号必须和节点的版本号dataVersion（linux）对应上
	@Test
	public void del3() throws Exception{
		String path = "/bb2/bb4";
		curatorZkClient.delete().guaranteed().withVersion(0).forPath(path);
	}
	
	//带版本号删除,版本号必须和节点的版本号dataVersion（linux）对应上
	@Test
	public void del4() throws Exception{
		String path = "/aa";
		curatorZkClient.delete().guaranteed().inBackground(new DeleteBackgroundCallback()).forPath(path);
	}
	//获取子节点
	@Test
	public void getChild1() throws Exception{
		String path = "/watcherTest";
		Stat stat = new Stat();
		List<String> nodeList = curatorZkClient.getChildren().storingStatIn(stat).forPath(path);
		for(String node:nodeList){
			System.out.println(node);
		}
	}
	
	//获去子节点并设置watcher
	@Test
	public void getChild2() throws Exception{
		String path = "/bb2";
		Stat stat = new Stat();
		Watcher watcher;
		List<String> nodeList = curatorZkClient.getChildren().usingWatcher(new CuratorWatcherTest()).forPath(path);
		for(String node:nodeList){
			System.out.println(node);
		}
		//设置的watcher只作用一次，这里要让线程先睡一阵子，不然设置的watcher没用
		Thread.sleep(500000);
	}
	
	//对节点进行监听
	@Test
	public void testWatcher() throws Exception{
		String path = "/watcherTest";
		final NodeCache nodeCache = new NodeCache(curatorZkClient, path);
		nodeCache.start(true);
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			
			public void nodeChanged() throws Exception {
				ChildData currentData = nodeCache.getCurrentData();
				String path2 = currentData.getPath();
				String data = new String(currentData.getData());
				System.out.println(path2+",data="+data);
			}
		});
		//设置的watcher只作用一次，这里要让线程先睡一阵子，不然设置的watcher没用
		Thread.sleep(500000);
	}
	
	//对节点进行监听
	@Test
	public void testChildWatcher() throws Exception{
		String path = "/watcherTest";
		final PathChildrenCache pathChildCache = new PathChildrenCache(curatorZkClient, path, true);
		System.out.println("开始时的子节点个数："+pathChildCache.getCurrentData().size());//获取子节点的个数
//		pathChildCache.start(StartMode.BUILD_INITIAL_CACHE);//同࠵初始化客户端的cache，及创建cache后，就从服务器端拉入对应的数据
//		pathChildCache.start(StartMode.NORMAL);//异࠵初始化cache
		pathChildCache.start(StartMode.POST_INITIALIZED_EVENT);//异࠵初始化，初始化完成触发事件PathChildrenCacheEvent.Type.INITIALIZED
		
		pathChildCache.getListenable().addListener(new PathChildrenCacheListener() {
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				System.out.println("子节点个数："+pathChildCache.getCurrentData().size());//获取子节点的个数
				if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
					System.out.println("子节点添加");
				}else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
					System.out.println("子节点删除");
				}else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
					System.out.println("子节点更新");
				}else if(event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)){
					System.out.println("子节点cache初始化数据完毕");
				}else{
					System.out.println("nothing happen!");
				}
				System.out.println("data："+ new String(event.getData().getData()));
				System.out.println("path："+ event.getData().getPath());
				System.out.println("-----------");
			}
		});
		
		Thread.sleep(500000);
	}
	
	

}
