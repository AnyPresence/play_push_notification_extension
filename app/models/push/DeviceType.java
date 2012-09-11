package models.push;

public enum DeviceType {
	android, ios;

	public static DeviceType fromDescription(String description) {
		if (description == null || description.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"description must be provided");
		}

		String lowerUa = description.toLowerCase();

		if (lowerUa.contains("iphone") || lowerUa.contains("ipad")
				|| lowerUa.contains("ipod")) {
			return ios;
		} else if (lowerUa.contains("android")) {
			return android;
		} else {
			throw new IllegalArgumentException(
					"Don't know how to match device type with description "
							+ description);
		}
	}
}