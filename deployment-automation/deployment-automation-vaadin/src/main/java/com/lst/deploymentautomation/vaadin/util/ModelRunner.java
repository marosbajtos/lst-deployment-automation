package com.lst.deploymentautomation.vaadin.util;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.page.RunModelView;
import com.whitestein.lsps.common.StringProperties;
import com.whitestein.lsps.engine.dto.ModelInstanceInfo;
import com.whitestein.lsps.engine.dto.Module;
import com.whitestein.lsps.engine.dto.ModuleCriteria;
import com.whitestein.lsps.engine.dto.ModuleList;
import com.whitestein.lsps.engine.ejb.ModelManagementServiceLocal;
import com.whitestein.lsps.human.dto.TodoList;
import com.whitestein.lsps.human.ejb.HumanActionsServiceLocal;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;

/**
 * Class for wrapping model running functions.
 * 
 * @author oko
 *
 */
public class ModelRunner {

	private static final Logger log = LoggerFactory.getLogger(RunModelView.class.getName());

	@EJB
	private ModelManagementServiceLocal modelManagementService;

	@EJB
	private HumanActionsServiceLocal humanActionsService;

	@EJB
	private TodoServiceLocal todoService;

	/**
	 * Simple constructor.
	 */
	public ModelRunner() {
		LspsAppConnectorImpl.INJECTOR_INSTANCE.inject(this);
	}

	/**
	 * Runs model specified by model name and version.
	 * If version is null, newest model is used.
	 * 
	 * @param name
	 * @param version
	 * @param ui 
	 */
	public void runModelByName(String name, String version, LspsUI ui) {
		ModuleCriteria criteria = new ModuleCriteria();
		criteria.setNamePatternCaseInsensitive(false);
		criteria.setNamePattern(name);
		criteria.setVersionPattern(version);

		ModuleList modules = modelManagementService.findModules(criteria);
		Long modelId = (long) 0;
		for (Module module : modules) {
			if (modelId < module.getId()) {
				modelId = module.getId();
			}
		}
		runModelById(modelId, ui);
	}

	/**
	 * Runs model specified by id.
	 * 
	 * @param modelId
	 * @param ui 
	 */
	public void runModelById(Long modelId, LspsUI ui) {

		ModelInstanceInfo info;
		try {
			info = humanActionsService.createModelInstance(modelId, new StringProperties(), true);

		} catch (Exception e) {
			ui.showErrorMessage("run.createProcessFailed", e);
			Utils.log(e, "could not create process", log);
			return;
		}

		try {
			long modelInstanceId = info.getId();
			TodoList nextTodos = todoService.startInterpretModelInstance(modelInstanceId);

			ui.showInfoMessage("run.createSuccessful", modelInstanceId);

			//either open next to-do or open homepage
			if (nextTodos.getData().size() == 1) {
				ui.openTodo(nextTodos.getData().get(0).getId());
			} else {
				ui.openHomePage();
			}

		} catch (Exception e) {
			ui.showErrorMessage("run.startProcessFailed", e, String.valueOf(info.getId()));
			Utils.log(e, "could not start process " + info.getId(), log);
		}
	}
}
