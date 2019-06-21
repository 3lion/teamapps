package org.teamapps.ux.component.charting.tree;

import org.jetbrains.annotations.NotNull;
import org.teamapps.data.extract.BeanPropertyExtractor;
import org.teamapps.data.extract.PropertyExtractor;
import org.teamapps.dto.UiClientRecord;
import org.teamapps.dto.UiComponent;
import org.teamapps.dto.UiEvent;
import org.teamapps.dto.UiTreeGraph;
import org.teamapps.dto.UiTreeGraphNode;
import org.teamapps.event.Event;
import org.teamapps.util.UiUtil;
import org.teamapps.ux.component.AbstractComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeGraph<RECORD> extends AbstractComponent {

	public final Event<TreeGraphNode<RECORD>> onNodeClicked = new Event<>();
	public final Event<NodeExpandedOrCollapsedEvent<RECORD>> onNodeExpandedOrCollapsed = new Event<>();

	private float zoomFactor;
	private List<TreeGraphNode<RECORD>> nodes = new ArrayList<>();
	private PropertyExtractor<RECORD> propertyExtractor = new BeanPropertyExtractor<>();

	public TreeGraph() {

	}

	@Override
	public UiComponent createUiComponent() {
		UiTreeGraph ui = new UiTreeGraph(createUiNodes(nodes));
		ui.setZoomFactor(zoomFactor);
		return ui;
	}

	private List<UiTreeGraphNode> createUiNodes(List<TreeGraphNode<RECORD>> nodes) {
		return nodes.stream()
				.map(node -> createUiNode(node))
				.collect(Collectors.toList());
	}

	@NotNull
	private UiTreeGraphNode createUiNode(TreeGraphNode<RECORD> node) {
		UiTreeGraphNode uiNode = new UiTreeGraphNode(node.getId(), node.getWidth(), node.getHeight());
		uiNode.setParentId(node.getParent() != null ? node.getParent().getId() : null);
		uiNode.setBackgroundColor(node.getBackgroundColor() != null ? UiUtil.createUiColor(node.getBackgroundColor()) : null);
		uiNode.setBorderColor(node.getBorderColor() != null ? UiUtil.createUiColor(node.getBorderColor()) : null);
		uiNode.setBorderWidth(node.getBorderWidth());
		uiNode.setBorderRadius(node.getBorderRadius());
		uiNode.setImage(node.getImage() != null ? node.getImage().createUiTreeGraphNodeImage() : null);
		uiNode.setIcon(node.getIcon() != null ? node.getIcon().createUiTreeGraphNodeIcon() : null);
		uiNode.setTemplate(node.getTemplate() != null ? node.getTemplate().createUiTemplate() : null);
		uiNode.setRecord(node.getRecord() != null ? createUiRecord(node) : null);
		uiNode.setConnectorLineColor(node.getConnectorLineColor() != null ? UiUtil.createUiColor(node.getConnectorLineColor()) : null);
		uiNode.setConnectorLineWidth(node.getConnectorLineWidth());
		uiNode.setDashArray(node.getDashArray());
		uiNode.setExpanded(node.isExpanded());
		return uiNode;
	}

	private UiClientRecord createUiRecord(TreeGraphNode<RECORD> node) {
		UiClientRecord uiClientRecord = new UiClientRecord();
		uiClientRecord.setValues(propertyExtractor.getValues(node.getRecord(), node.getTemplate().getDataKeys()));
		return uiClientRecord;
	}

	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
		queueCommandIfRendered(() -> new UiTreeGraph.SetZoomFactorCommand(getId(), zoomFactor));
	}

	public void setNodes(List<TreeGraphNode<RECORD>> nodes) {
		this.nodes.clear();
		this.nodes.addAll(nodes);
		queueCommandIfRendered(() -> new UiTreeGraph.SetNodesCommand(getId(), createUiNodes(nodes)));
	}

	public void addNode(TreeGraphNode<RECORD> node) {
		this.nodes.add(node);
		queueCommandIfRendered(() -> new UiTreeGraph.AddNodeCommand(getId(), createUiNode(node)));
	}

	public void removeNode(TreeGraphNode<RECORD> node) {
		this.nodes.remove(node);
		queueCommandIfRendered(() -> new UiTreeGraph.RemoveNodeCommand(getId(), node.getId()));
	}

	@Override
	public void handleUiEvent(UiEvent event) {
		switch (event.getUiEventType()) {
			case UI_TREE_GRAPH_NODE_CLICKED:
				UiTreeGraph.NodeClickedEvent clickEvent = (UiTreeGraph.NodeClickedEvent) event;
				this.nodes.stream()
						.filter(node -> node.getId().equals(clickEvent.getNodeId()))
						.findFirst()
						.ifPresent(onNodeClicked::fire);
				break;
			case UI_TREE_GRAPH_NODE_EXPANDED_OR_COLLAPSED:
				UiTreeGraph.NodeExpandedOrCollapsedEvent e = (UiTreeGraph.NodeExpandedOrCollapsedEvent) event;
				this.nodes.stream()
						.filter(node -> node.getId().equals(e.getNodeId()))
						.findFirst()
						.ifPresent(node -> onNodeExpandedOrCollapsed.fire(new NodeExpandedOrCollapsedEvent<>(node, e.getExpanded())));
				break;
		}
	}
}
