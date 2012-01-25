package daris.client.model.object.messages;

import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.lock.LockToken;

public class DObjectLockToken implements LockToken {

	private String _id;
	private boolean _ok;

	public DObjectLockToken(String id) {

		_id = id;
		_ok = true;
	}

	@Override
	public int renewPeriodInSeconds() {

		return 30;
	}

	@Override
	public boolean renew() {

		if (!_ok) {
			return false;
		}

		new DObjectLockRenew(_id, 60).send(new ObjectMessageResponse<Boolean>() {
			public void responded(Boolean renewed) {

				_ok = renewed;
			}
		});

		return true;
	}

	@Override
	public void release() {

		if (_ok) {
			new DObjectLockRelease(_id).send(new ObjectMessageResponse<Null>() {
				public void responded(Null r) {

					_ok = false;
				}
			});
		}

	}

}
