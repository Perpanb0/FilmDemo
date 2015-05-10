package dp.ws.popcorntime.model.videoinfo;

public class Episode {

	public String id;
	public String seq;
	public String name;

	public Episode(String id, String seq, String name) {
		this.id = id;
		this.seq = seq;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	};
}