package dev.stocky37.xiv.actions.data;

import com.google.common.collect.Lists;
import dev.stocky37.xiv.actions.core.ActionService;
import dev.stocky37.xiv.actions.xivapi.json.XivApiClassJob;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class XivApiJobConverter implements Function<XivApiClassJob, Job> {

	private final ActionService actionService;
	private final boolean enrichActions;

	public XivApiJobConverter(ActionService actionService, boolean enrichActions) {
		this.actionService = actionService;
		this.enrichActions = enrichActions;
	}

	@Override
	public Job apply(XivApiClassJob classJob) {
		return new Job(
			String.valueOf(classJob.ID()),
			classJob.Name(),
			classJob.Abbreviation(),
			classJob.Icon(),
			category(classJob),
			type(classJob),
			role(classJob),
			classJob.JobIndex(),
			classJob.IsLimitedJob() != 0,
			enrichActions ? actions(classJob) : Lists.newArrayList()
		);
	}

	private List<Action> actions(XivApiClassJob classJob) {
		return Collections.unmodifiableList(actionService.findForJob(classJob.Abbreviation()));
	}

	private Job.Category category(XivApiClassJob classJob) {
		return switch(classJob.ClassJobCategoryTargetID()) {
			case 30 -> Job.Category.DOW;
			case 31 -> Job.Category.DOM;
			case 32 -> Job.Category.DOL;
			case 33 -> Job.Category.DOH;
			default -> throw new RuntimeException(
				"Unknown category: " + classJob.ClassJobCategoryTargetID());
		};
	}

	private Job.Type type(XivApiClassJob classJob) {
		return classJob.JobIndex() > 0 ? Job.Type.JOB : Job.Type.CLASS;
	}

	private Job.Role role(XivApiClassJob classJob) {
		return switch(classJob.Role()) {
			case 0 -> Job.Role.NON_BATTLE;
			case 1 -> Job.Role.TANK;
			case 2 -> Job.Role.MELEE_DPS;
			case 3 -> Job.Role.RANGED_DPS;
			case 4 -> Job.Role.HEALER;
			default -> throw new RuntimeException("Unknown role: " + classJob.Role());
		};
	}

	@Override
	public String toString() {
		return "XivApiJobConverter{" +
			"enrichActions=" + enrichActions +
			'}';
	}
}
