package person.pluto.tbds;

import lombok.Data;
 
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
 
@Data
public class IpInfo {
 
	private String ip;
	/**
	 * 国家
	 */
	private String country;
 
	/**
	 * 区域
	 */
	private String region;
 
	/**
	 * 省
	 */
	private String province;
 
	/**
	 * 城市
	 */
	private String city;
 
	/**
	 * 运营商
	 */
	private String isp;
 
	/**
	 * 拼接完整的地址
	 * @return address
	 */
	public String getAddress() {
		Set<String> regionSet = new LinkedHashSet<>();
		regionSet.add(country);
		regionSet.add(region);
		regionSet.add(province);
		regionSet.add(city);
		regionSet.removeIf(Objects::isNull);
		return String.join("", regionSet);
	}
 
	/**
	 * 拼接完整的地址
	 * @return address
	 */
	public String getAddressAndIsp() {
		Set<String> regionSet = new LinkedHashSet<>();
		regionSet.add(country);
		regionSet.add(region);
		regionSet.add(province);
		regionSet.add(city);
		regionSet.add(isp);
		regionSet.removeIf(Objects::isNull);
		return String.join("", regionSet);
	}
 
}
