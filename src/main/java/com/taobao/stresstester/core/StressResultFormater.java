package com.taobao.stresstester.core;

import java.io.Writer;

public interface StressResultFormater {

	void format(StressResult stressResult, Writer writer);

}
