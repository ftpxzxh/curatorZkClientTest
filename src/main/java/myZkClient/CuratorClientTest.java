package myZkClient;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
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
	@Before
	public void getCuratorClient2() {
		RetryPolicy retryPolicy = new RetryNTimes(5, 5000);//连接不上时，每间隔5s重试一次，共重试5次
		int sessionTimeoutMs = 60000;//会话超时时间，默认是60s
		int connectionTimeoutMs = 15000;//连接超时时间，默认是15s
		curatorZkClient = CuratorFrameworkFactory.newClient(connectString , sessionTimeoutMs , connectionTimeoutMs , retryPolicy);
		curatorZkClient.start();
	}
	//Fluent风格的方式返回curator客户端
//	@Before
//	public void getCuratorClient3() {
////		第一次重试的间隔是1s，按照指数倍增的重试间隔进行重连，重连最大次数是3；
////		例如第一次连不上，间隔1s后重连；还是连不上，再间隔2s进行重连；还是连不上，再间隔4s后重连；还是连接不上，这个时候已经重试了3次了，就不再重试了
//		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
//		curatorZkClient = CuratorFrameworkFactory.builder()
//				.connectString(connectString)
//				.sessionTimeoutMs(10000)		//会话超时时间，单位为毫秒，默认60000ms
//				.connectionTimeoutMs(10000)     //连接创建超时时间，单位为毫秒，默认是15000ms
//				.retryPolicy(retryPolicy)		//重试策略
//				.namespace("curatorClientTest") //设置该客户端的根目录
//				.build();
//		curatorZkClient.start();
//	}
	
	@After
	public void closeCurator(){
		if(null!=curatorZkClient){
			curatorZkClient.close();
		}
	}
	
	//创建一个普通的节点，/curatorClientTest/aa并把它的值设置为aa
	@Test
	public void createNode1() throws Exception{
		String path = "/aa";//path必须以/开头，否则会报错
		String data = "aa";
		curatorZkClient.create().forPath(path , data.getBytes());	
	}
	
	//递归创建/curatorClientTest/bb1/bb2节点，并把bb2的节点值设置为bb2
	@Test
	public void createNode2() throws Exception{
		String path = "/bb1/bb2";
		String data = "bb2";
		curatorZkClient.create().creatingParentsIfNeeded().forPath(path , data.getBytes());	
	}
	
	//创建一个持久化节点withMode(CreateMode.PERSISTENT)
	@Test
	public void createNode3() throws Exception{
		String path = "/cc";
		String data = "cc";
		curatorZkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path , data.getBytes());	
	}
	//创建一个临时节点，临时节点会在
	@Test
	public void createNode4() throws Exception{
		String path = "/dd";
		String data = "dd";
		curatorZkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path , data.getBytes());	
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
		String path = "/aa";
		String data = "aa1";
		curatorZkClient.setData()
		.withVersion(1)//特定版本号，可不加；如果加上特定版本号，则必须和服务器的版本号对应得上
		.forPath(path, data.getBytes());
	}
	
	//删除一个节点
	@Test
	public void del1() throws Exception{
//		String path = "/curatorTest2/bb";
		String path = "/aa";
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
		String path = "/aa";
		curatorZkClient.delete().guaranteed().withVersion(1).forPath(path);
	}

}
