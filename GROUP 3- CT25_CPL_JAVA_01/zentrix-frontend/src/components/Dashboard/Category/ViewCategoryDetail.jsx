import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { ChevronDown, ChevronUp, ChevronRight } from "lucide-react";
import { useState } from "react";

const ViewCategoryDetail = ({ isOpen, onClose, category, categories, onExpandParent, onExpandChildren }) => {
    const [isChildrenExpanded, setIsChildrenExpanded] = useState(false);

    if (!category) return null;

    // Find the parent category
    const parentCategory = category.parentCateId
        ? categories.find((cat) => cat.cateId === category.parentCateId.cateId)
        : null;

    // Find all child categories of the current category
    const childCategories = categories.filter((cat) => 
        cat.parentCateId && cat.parentCateId.cateId === category.cateId
    );

    // Handle expanding the parent category in the parent component
    const handleExpandParent = () => {
        if (parentCategory && onExpandParent) {
            onExpandParent(parentCategory.cateId);
        }
    };

    // Handle expanding all child categories in the parent component
    const handleExpandChildren = () => {
        if (childCategories.length > 0 && onExpandChildren) {
            onExpandChildren(category.cateId);
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[34rem] rounded-xl shadow-2xl">
                <DialogHeader>
                    <DialogTitle className="text-left text-2xl font-bold text-black">Category Detail</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 py-5">
                    {/* Category ID */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="cateId" className="text-right font-medium text-black">ID</Label>
                        <Input 
                            id="cateId" 
                            value={category.cateId || ""} 
                            className="col-span-3 !text-black font-bold text-xl bg-white border-2 border-gray-600 shadow-xl rounded-md" 
                            disabled 
                        />
                    </div>

                    {/* Category Name */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="cateName" className="text-right font-medium text-black">Category Name</Label>
                        <Input 
                            id="cateName" 
                            value={category.cateName || ""} 
                            className="col-span-3 !text-black font-bold text-xl bg-white border-2 border-gray-600 shadow-xl rounded-md" 
                            disabled 
                        />
                    </div>

                    {/* Parent Category */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="parentCateId" className="text-right font-medium text-black">Parent Category</Label>
                        <div className="col-span-3 flex items-center gap-2">
                            <Input
                                id="parentCateId"
                                value={parentCategory ? parentCategory.cateName : "None"}
                                className="flex-1 !text-black font-bold text-xl bg-white border-2 border-gray-600 shadow-xl rounded-md"
                                disabled
                            />
                            {parentCategory && (
                                <Button
                                    onClick={handleExpandParent}
                                    className="bg-blue-500 hover:bg-blue-600 text-white p-2"
                                >
                                    <ChevronRight className="h-5 w-5" />
                                </Button>
                            )}
                        </div>
                    </div>

                    {/* Child Categories */}
                    <div className="grid gap-4">
                        <div className="flex justify-between items-center">
                            <Label className="text-lg font-medium text-black">Child Categories</Label>
                            {childCategories.length > 0 && (
                                <Button
                                    onClick={() => setIsChildrenExpanded(!isChildrenExpanded)}
                                    className="bg-blue-500 hover:bg-blue-600 text-white text-sm px-3 py-1"
                                >
                                    {isChildrenExpanded ? <ChevronUp className="h-5 w-5" /> : <ChevronDown className="h-5 w-5" />}
                                </Button>
                            )}
                        </div>
                        {childCategories.length === 0 ? (
                            <p className="text-gray-600 text-sm">No child categories found.</p>
                        ) : isChildrenExpanded && (
                            <div className="overflow-x-auto">
                                <table className="w-full border-collapse border border-gray-300">
                                    <thead>
                                        <tr className="bg-blue-500 text-white">
                                            <th className="border p-2 text-left text-sm">ID</th>
                                            <th className="border p-2 text-left text-sm">Category Name</th>
                                            <th className="border p-2 text-left text-sm">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {childCategories.map((child) => (
                                            <tr key={child.cateId} className="hover:bg-gray-100">
                                                <td className="border p-2 text-sm">{child.cateId}</td>
                                                <td className="border p-2 text-sm">{child.cateName}</td>
                                                <td className="border p-2 text-sm">
                                                    <Button
                                                        onClick={() => onExpandChildren(child.cateId)}
                                                        className="bg-blue-500 hover:bg-blue-600 text-white p-1"
                                                    >
                                                        <ChevronRight className="h-4 w-4" />
                                                    </Button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                        {childCategories.length > 0 && (
                            <Button
                                onClick={handleExpandChildren}
                                className="mt-2 bg-blue-500 hover:bg-blue-600 text-white p-2"
                            >
                                <ChevronRight className="h-5 w-5" />
                            </Button>
                        )}
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>Close</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default ViewCategoryDetail;