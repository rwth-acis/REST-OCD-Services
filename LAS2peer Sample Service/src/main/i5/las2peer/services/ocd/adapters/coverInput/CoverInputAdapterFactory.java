package i5.las2peer.services.ocd.adapters.coverInput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

public class CoverInputAdapterFactory implements SimpleFactory<CoverInputAdapter, CoverInputFormat>{

	@Override
	public CoverInputAdapter getInstance(CoverInputFormat inputFormat) throws InstantiationException, IllegalAccessException {
		return inputFormat.getAdapterClass().newInstance();
	}

}
