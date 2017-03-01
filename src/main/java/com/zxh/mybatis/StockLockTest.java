package com.zxh.mybatis;

import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.zxh.mybatis.po.ProductBase;
import com.zxh.thread.MyThread;

import lock.exclusive.MyLockUtil;


public class StockLockTest {
	private SqlSessionFactory sqlSessionFactory = null;
	
	public StockLockTest() {
		try {
			getSqlSessionFactory();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getSqlSessionFactory() throws IOException{
		// mybatis配置文件
		String resource = "SqlMapConfig.xml";
		// 得到配置文件流
		InputStream inputStream = Resources.getResourceAsStream(resource);

		// 创建会话工厂，传入mybatis的配置文件信息
		sqlSessionFactory = new SqlSessionFactoryBuilder()
				.build(inputStream);
	}
	
	public void updateUserTest() throws IOException {
		// 通过工厂得到SqlSession
		SqlSession sqlSession = sqlSessionFactory.openSession();
		MyLockUtil lockUtil = new MyLockUtil();
		lockUtil.getExclusiveLock();
		ProductBase productBase = sqlSession.selectOne("com.zxh.mybatis.mapper.ProductBaseMapper.selectByPrimaryKey",1);
		productBase.setStock(productBase.getStock()-1);
		if(productBase.getStock()<0){
			System.out.println(Thread.currentThread().getName()+"库存已经卖完了");
			lockUtil.unLockForExclusive();
			return;
		}
 		sqlSession.update("com.zxh.mybatis.mapper.ProductBaseMapper.updateByPrimaryKey", productBase);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 提交事务
		sqlSession.commit();
		System.out.println(Thread.currentThread().getName()+"买到了一件商品");
		lockUtil.unLockForExclusive();
		// 关闭会话
		sqlSession.close();

	}
	public static void main(String[] args) {
		MyThread mythread = new MyThread();
		new Thread(mythread).start();
		new Thread(mythread).start();
		new Thread(mythread).start();
		new Thread(mythread).start();
		new Thread(mythread).start();
		new Thread(mythread).start();
		new Thread(mythread).start();
		new Thread(mythread).start();
	}

}

