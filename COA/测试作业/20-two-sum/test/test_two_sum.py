import unittest
from src import two_sum


class Test(unittest.TestCase):

    def test1(self):
        actual = two_sum.twoSum([3, 2, 4], 6)
        expect = [1, 2]
        self.assertEqual(expect, actual)

    def test2(self):
        actual = two_sum.twoSum([3, 4, 3], 6)
        expect = [0, 2]
        self.assertEqual(expect, actual)

    def test3(self):
        actual = two_sum.twoSum([2, 7, 11, 15], 9)
        expect = [0, 1]
        self.assertEqual(expect, actual)

    def test4(self):
        actual = two_sum.twoSum([2, 7, 11, 15], 22)
        expect = [1, 3]
        self.assertEqual(expect, actual)