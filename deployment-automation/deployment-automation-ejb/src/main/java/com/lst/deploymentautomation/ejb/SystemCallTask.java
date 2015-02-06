package com.lst.deploymentautomation.ejb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

import org.apache.commons.io.IOUtils;

import com.whitestein.lsps.common.ErrorException;
import com.whitestein.lsps.engine.AbstractAsynchronousExecutionTask;
import com.whitestein.lsps.engine.lang.TaskContext;
import com.whitestein.lsps.lang.Decimal;
import com.whitestein.lsps.lang.exec.CollectionHolder;
import com.whitestein.lsps.lang.exec.ReferenceHolder;

@Stateless
public class SystemCallTask extends AbstractAsynchronousExecutionTask {

	public static String CMD_ARRAY_PARAM = "command";
	public static String OUTPUT_PARAM = "output";
	public static String ERR_PARAM = "error";
	public static String EXIT_VALUE_PARAM = "exitValue";

	@Override
	public Serializable collectDataForExecution(TaskContext context) throws ErrorException {
		CollectionHolder holder = (CollectionHolder) context.getParameter(CMD_ARRAY_PARAM);
		return new ArrayList<Object>(holder);
	}

	@Override
	public boolean executeAsynchronously(TaskContext arg0) throws ErrorException {
		return true;
	}

	@Override
	public Class<? extends AbstractAsynchronousExecutionTask> getImplementationClass() {
		return SystemCallTask.class;
	}

	@Override
	public Serializable processDataAsynchronously(Serializable cmdArrayData) {
		try {
			List<String> cmdArray = (List<String>) cmdArrayData;
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(cmdArray.toArray(new String[] {}));
			p.waitFor();

			return new SystemCallResult(
					IOUtils.toString(p.getInputStream()),
					IOUtils.toString(p.getErrorStream()),
					p.exitValue());

		} catch (Exception e) {
			return new SystemCallResult(null, e.toString(), -1);
		}
	}

	@Override
	public void processExecutionResult(TaskContext context, Serializable resultData) throws ErrorException {
		ReferenceHolder output = (ReferenceHolder) context.getParameter(OUTPUT_PARAM);
		ReferenceHolder err = (ReferenceHolder) context.getParameter(ERR_PARAM);
		ReferenceHolder exitValue = (ReferenceHolder) context.getParameter(EXIT_VALUE_PARAM);

		SystemCallResult result = (SystemCallResult) resultData;

		output.setValue(result.getOutput());
		err.setValue(result.getError());
		exitValue.setValue(new Decimal(result.getExitValue()));

	}

	private static class SystemCallResult implements Serializable {

		private String output, error;
		private int exitValue;

		public SystemCallResult(String output, String error, int exitValue) {
			super();
			this.output = output;
			this.error = error;
			this.exitValue = exitValue;
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			this.output = output;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public int getExitValue() {
			return exitValue;
		}

		public void setExitValue(int exitValue) {
			this.exitValue = exitValue;
		}
	}

}
