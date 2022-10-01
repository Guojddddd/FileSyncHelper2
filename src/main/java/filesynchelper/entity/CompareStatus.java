package filesynchelper.entity;

/**
 * @author guo
 * 比对结果
 */
public enum CompareStatus {
	/**
	 * 相同
	 */
	STATUS_SAME(0),
	/**
	 * 多余
	 */
	STATUS_OVER(1),
	/**
	 * 不同大小
	 */
	STATUS_DIFF_SIZE(2),
	/**
	 * 不同类型
	 */
	STATUS_DIFF_TYPE(3),
	/**
	 * 缺失
	 */
	STATUS_LOST(4);

	private int code;

	CompareStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
