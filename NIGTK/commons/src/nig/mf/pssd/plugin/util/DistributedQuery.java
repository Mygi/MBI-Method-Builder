package nig.mf.pssd.plugin.util;

public class DistributedQuery {

	/**
	 * The result asset type. It is used to specify which type(s) of assets
	 * should be included in the result. Maybe there will be other types in the
	 * future, so 'all' rather than 'both'.
	 * 
	 */
	public enum ResultAssetType {
		primary, replica, all;
		public static ResultAssetType instantiate(String type) throws Throwable {

			if (primary.toString().equalsIgnoreCase(type)) {
				return primary;
			} else if (replica.toString().equalsIgnoreCase(type)) {
				return replica;
			} else if (all.toString().equalsIgnoreCase(type)) {
				return all;
			} else {
				throw new Exception("Invalid type: " + type);
			}
		}

		public static String[] stringValues() {

			ResultAssetType[] vs = values();
			String[] svs = new String[vs.length];
			for (int i = 0; i < vs.length; i++) {
				svs[i] = vs[i].toString();
			}
			return svs;
		}
	};

	/**
	 * 
	 * When collections may contain primaries and replicas. The policy filter
	 * states how we filter this collection
	 * 
	 */
	public enum ResultFilterPolicy {
		primary_then_any_replica, // Use the primary in preference to any
									// replica
		primary_then_nearest_replica, // Use the primary in preference to the
										// nearest replica
		any, // Use the first object that is encountered, primary or replica
		nearest, // Use the nearest (proute) object that is encountered, primary
					// or replica
		none; // Don't filter

		@Override
		public String toString() {

			return super.toString().replace('_', '-');
		}

		public static ResultFilterPolicy instantiate(String policy) throws Throwable {

			if (primary_then_any_replica.toString().equalsIgnoreCase(policy)) {
				return primary_then_any_replica;
			} else if (primary_then_nearest_replica.toString().equalsIgnoreCase(policy)) {
				// return primary_then_nearest_replica;
			} else if (any.toString().equalsIgnoreCase(policy)) {
				// return any;
			} else if (nearest.toString().equalsIgnoreCase(policy)) {
				// return nearest;
			} else if (none.toString().equalsIgnoreCase(policy)) {
				return none;
			}
			throw new Exception("Policy: " + policy + " is not supported");
		}

		public static String[] stringValues() {

			ResultFilterPolicy[] vs = values();
			String[] svs = new String[vs.length];
			for (int i = 0; i < vs.length; i++) {
				svs[i] = vs[i].toString();
			}
			return svs;
		}

	};

	public static String resultAssetTypePredicate(ResultAssetType assetType) {

		if (assetType == ResultAssetType.primary) {
			return "rid hasno value";
		} else if (assetType == ResultAssetType.replica) {
			return "rid has value";
		} else {
			return "";
		}
	}

	public static void appendResultAssetTypePredicate(String queryString, ResultAssetType assetType) {

		String predicate = resultAssetTypePredicate(assetType);
		if (!predicate.equals("")) {
			queryString += " and (" + predicate + ")";
		}
	}

}
