package filesynchelper.entity;

/**
 * @author guo
 * 文件类型
 */
public enum FileType {
	/**
	 * 文件
	 */
	FILE(0, "文件"),
	/**
	 * 目录
	 */
	DIR(1, "目录"),
	/**
	 * 目录
	 */
	UNKNOWN(2, "未知");

	private int code;
	private String name;

	FileType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
