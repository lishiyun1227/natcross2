package person.pluto.tbds;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.lionsoul.ip2region.xdb.Searcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
 
/**
 * Ip2regionSearcher
 *
 * @author healthy
 */
@Slf4j
public class IPUtil {
 
	public static void main(String[] args) {
		IpInfo info = search("36.112.108.4");
		System.out.println(info.getAddress());
		System.out.println(info.getAddressAndIsp());
		System.out.println(search("162.142.125.217").getAddress());
	}
	
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\|");
 
	private static Searcher searcher;
	
	// private static String DBPATH = "D:/Tools/deploy/ip2region.xdb";
	private static String DBPATH = "/home/tbds/tbds/data/ip2region.xdb";
	
	static int secs = 10;
	static int conn = 9;	// TBDS网页第一次打开会建立6个连接，需大于此值
	

	static LoadingCache<String, AtomicInteger> frequencies = CacheBuilder.newBuilder().expireAfterWrite(secs, TimeUnit.SECONDS).build(new CacheLoader<String, AtomicInteger>() {
		public AtomicInteger load(String key) throws Exception {
			return new AtomicInteger(1);
		}
	});
	
	
	static Cache<Object, Object> banedIps = CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build();
	
	static {
		init();
	}
	
	private static void init() {
		if(searcher == null) {
			File dbFile = new File(DBPATH);
			if(dbFile.exists()) {
				try {
					searcher = Searcher.newWithFileOnly(DBPATH);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException("IP数据库文件不存在！");
			}
		}
	}
 
	public static boolean isValid(IpInfo info) {
		if(info != null) {
			if("中国".equals(info.getCountry()) || "内网IP".equals(info.getCity())) {
				if(banedIps.getIfPresent(info.getIp()) == null) {
					return true;
				}
			}
		}
		return false;
	}
	
	@SneakyThrows
	public static String searchStr(String ip) {
		AtomicInteger count = frequencies.get(ip);
		int get = count.getAndIncrement();
		if(get > conn) {
			banedIps.put(ip, new Integer(get));
			log.error("banned {}", ip);
		}
		return searcher.search(ip);
	}
 
	public static IpInfo search(String ip) {
		String region = searchStr(ip);
		if (region == null) {
			return null;
		}
		IpInfo ipInfo = new IpInfo();
		ipInfo.setIp(ip);
		String[] splitInfos = SPLIT_PATTERN.split(region);
		// 补齐5位
		if (splitInfos.length < 5) {
			splitInfos = Arrays.copyOf(splitInfos, 5);
		}
		ipInfo.setCountry(filterZero(splitInfos[0]));
		ipInfo.setRegion(filterZero(splitInfos[1]));
		ipInfo.setProvince(filterZero(splitInfos[2]));
		ipInfo.setCity(filterZero(splitInfos[3]));
		ipInfo.setIsp(filterZero(splitInfos[4]));
		return ipInfo;
	}
 
	/**
	 * 数据过滤，因为 ip2Region 采用 0 填充的没有数据的字段
	 * @param info info
	 * @return info
	 */
	private static String filterZero(String info) {
		// null 或 0 返回 null
		if (info == null || BigDecimal.ZERO.toString().equals(info)) {
			return null;
		}
		return info;
	}
}
