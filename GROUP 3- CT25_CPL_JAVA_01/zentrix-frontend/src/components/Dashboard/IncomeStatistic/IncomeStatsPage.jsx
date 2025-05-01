"use client";

import * as React from "react";
import { useState, useEffect } from "react";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";
import { Pie, PieChart } from "recharts";
import { TrendingUp } from "lucide-react";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {
    ChartContainer,
    ChartLegend,
    ChartLegendContent,
    ChartTooltip,
    ChartTooltipContent,
} from "@/components/ui/chart";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import axios from "axios";

const areaChartConfig = {
    visitors: {
        label: "Visitors",
    },
    importCost: {
        label: "Import Cost (VND)  ",
        color: "#FF6B6B", // Màu đỏ nhạt
    },
    exportRevenue: {
        label: "Export Revenue (VND)",
        color: "#4ECDC4", // Màu xanh ngọc
    },
};

const pieChartConfig = {
    visitors: {
        label: "Visitors",
    },
    importCost: {
        label: "Import Cost (VND) ",
        color: "#FF6B6B", // Màu đỏ nhạt
    },
    exportRevenue: {
        label: "Export Revenue (VND) ",
        color: "#4ECDC4", // Màu xanh ngọc
    },
};

export function IncomeStatsPage() {
    const [chartData, setChartData] = useState([]);
    const [timeRange, setTimeRange] = React.useState("90d");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [importResponse, exportResponse] = await Promise.all([
                    axios.get("http://localhost:6789/api/v1/stocks", {
                        withCredentials: "true"
                    }),
                    axios.get("http://localhost:6789/api/v1/orders?page=0&size=10000", {
                        withCredentials: "true"
                    }),
                ]);

                const importStocks = importResponse.data.content || [];
                const exportStocks = (exportResponse.data.content || []).filter(item => [2, 3, 5].includes(item.status));
                console.log(exportStocks)
                importStocks.sort((a, b) => new Date(a.importDate) - new Date(b.importDate));

                const dailyData = {};
                importStocks.forEach(stock => {
                    const date = new Date(stock.importDate);
                    const formattedDate = date.toISOString().split("T")[0];
                    dailyData[formattedDate] = dailyData[formattedDate] || { importCost: 0, exportRevenue: 0 };
                    dailyData[formattedDate].importCost += stock.stockDetails?.reduce((s, d) => s + (d.importPrice * d.stockQuantity || 0), 0) || 0;
                });
                exportStocks.forEach(order => {
                    const date = new Date(order.createdAt);
                    const formattedDate = date.toISOString().split("T")[0];
                    dailyData[formattedDate] = dailyData[formattedDate] || { importCost: 0, exportRevenue: 0 };
                    dailyData[formattedDate].exportRevenue += order.orderDetails?.reduce((s, d) => s + (d.unitPrice * d.quantity || 0), 0) || 0;
                });

                const result = Object.entries(dailyData)
                    .map(([date, data]) => ({ date, ...data }))
                    .sort((a, b) => new Date(a.date) - new Date(b.date));
                setChartData(result);
                setLoading(false);
            } catch (err) {
                setError("Failed to fetch data");
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const filteredData = chartData.filter((item) => {
        const date = new Date(item.date);
        const referenceDate = new Date();
        let daysToSubtract = 90;
        if (timeRange === "30d") {
            daysToSubtract = 30;
        } else if (timeRange === "7d") {
            daysToSubtract = 7;
        }
        const startDate = new Date(referenceDate);
        startDate.setDate(startDate.getDate() - daysToSubtract);
        return date.toISOString().split("T")[0] >= startDate.toISOString().split("T")[0];
    });

    const pieChartData = [
        {
            name: "Import Cost",
            value: filteredData.reduce((sum, item) => sum + (item.importCost || 0), 0),
            fill: pieChartConfig.importCost.color,
        },
        {
            name: "Export Revenue",
            value: filteredData.reduce((sum, item) => sum + (item.exportRevenue || 0), 0),
            fill: pieChartConfig.exportRevenue.color,
        },
    ].filter(item => item.value > 0);

    const formatDateForXAxis = (value) => {
        const date = new Date(value);
        return date.toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
        });
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div className="space-y-6">
            {/* Area Chart */}
            <Card>
                <CardHeader className="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row">
                    <div className="grid flex-1 gap-1 text-center sm:text-left">
                        <CardTitle>Income Overview - Stacked Expanded</CardTitle>
                        <CardDescription>Showing income statistics for available days</CardDescription>
                    </div>
                    <Select value={timeRange} onValueChange={setTimeRange}>
                        <SelectTrigger
                            className="w-[160px] rounded-lg sm:ml-auto"
                            aria-label="Select a value"
                        >
                            <SelectValue placeholder="Last 3 months" />
                        </SelectTrigger>
                        <SelectContent className="rounded-xl">
                            <SelectItem value="90d" className="rounded-lg">
                                Last 3 months
                            </SelectItem>
                            <SelectItem value="30d" className="rounded-lg">
                                Last 30 days
                            </SelectItem>
                            <SelectItem value="7d" className="rounded-lg">
                                Last 7 days
                            </SelectItem>
                        </SelectContent>
                    </Select>
                </CardHeader>
                <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
                    <ChartContainer
                        config={areaChartConfig}
                        className="aspect-auto h-[250px] w-full"
                    >
                        <AreaChart data={filteredData}>
                            <defs>
                                <linearGradient id="fillImportCost" x1="0" y1="0" x2="0" y2="1">
                                    <stop
                                        offset="5%"
                                        stopColor={areaChartConfig.importCost.color}
                                        stopOpacity={0.8}
                                    />
                                    <stop
                                        offset="95%"
                                        stopColor={areaChartConfig.importCost.color}
                                        stopOpacity={0.1}
                                    />
                                </linearGradient>
                                <linearGradient id="fillExportRevenue" x1="0" y1="0" x2="0" y2="1">
                                    <stop
                                        offset="5%"
                                        stopColor={areaChartConfig.exportRevenue.color}
                                        stopOpacity={0.8}
                                    />
                                    <stop
                                        offset="95%"
                                        stopColor={areaChartConfig.exportRevenue.color}
                                        stopOpacity={0.1}
                                    />
                                </linearGradient>
                            </defs>
                            <CartesianGrid vertical={false} stroke="#e0e0e0" />
                            <XAxis
                                dataKey="date"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                minTickGap={32}
                                tickFormatter={formatDateForXAxis}
                            />
                            <YAxis tickFormatter={(value) => `${value.toLocaleString()}`} />
                            <ChartTooltip
                                cursor={false}
                                content={
                                    <ChartTooltipContent
                                        labelFormatter={formatDateForXAxis}
                                        indicator="dot"
                                    />
                                }
                            />
                            <Area
                                dataKey="exportRevenue"
                                type="natural"
                                fill="url(#fillExportRevenue)"
                                stroke={areaChartConfig.exportRevenue.color}
                                stackId="a"
                            />
                            <Area
                                dataKey="importCost"
                                type="natural"
                                fill="url(#fillImportCost)"
                                stroke={areaChartConfig.importCost.color}
                                stackId="a"
                            />
                            <ChartLegend content={<ChartLegendContent />} />
                        </AreaChart>
                    </ChartContainer>
                </CardContent>
            </Card>

            {/* Pie Chart */}
            <Card className="flex flex-col">
                <CardHeader className="items-center pb-0">
                    <CardTitle>Pie Chart - Income Distribution</CardTitle>
                    <CardDescription>January - {new Date().toLocaleDateString("en-US", { month: "long", year: "numeric" })}</CardDescription>
                </CardHeader>
                <CardContent className="flex-1 pb-0">
                    <ChartContainer
                        config={pieChartConfig}
                        className="mx-auto aspect-square max-h-[800px] pb-0 [&_.recharts-pie-label-text]:fill-foreground"
                    >
                        <PieChart>
                            <ChartTooltip content={<ChartTooltipContent hideLabel />} />
                            <Pie
                                data={pieChartData}
                                dataKey="value"
                                nameKey="name"
                                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                            />
                        </PieChart>
                    </ChartContainer>
                </CardContent>
            </Card>
        </div>
    );
}

export default IncomeStatsPage;