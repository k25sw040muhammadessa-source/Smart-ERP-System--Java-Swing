package javaapplication1.erp.ui.dashboard;

/**
 * Data source abstraction for dashboard widgets.
 * Integration point: replace demo implementation with DB-backed service calls.
 */
public interface DashboardDataProvider {

    DashboardModels.DashboardSnapshot loadSnapshot(String filterType, String query) throws Exception;
}
